package com.jhh.jhs.loan.service.channel;

import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.capital.HaierCallBackService;
import com.jhh.jhs.loan.api.channel.TradePayService;
import com.jhh.jhs.loan.api.constant.Constants;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.AgentDeductBatchRequest;
import com.jhh.jhs.loan.api.entity.capital.TradeBatchVo;
import com.jhh.jhs.loan.api.entity.capital.TradeVo;
import com.jhh.jhs.loan.common.enums.AgentDeductResponseEnum;
import com.jhh.jhs.loan.common.enums.PayChannelEnum;
import com.jhh.jhs.loan.common.util.RedisConst;
import com.jhh.jhs.loan.constant.HaierConstants;
import com.jhh.jhs.loan.entity.HaierDeductVo;
import com.jhh.jhs.loan.entity.HaierPayVo;
import com.jhh.jhs.loan.entity.HaierRefundVo;
import com.jhh.jhs.loan.entity.app.Bank;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.entity.manager.BankList;
import com.jhh.jhs.loan.mapper.app.BankMapper;
import com.jhh.jhs.loan.mapper.app.PersonMapper;
import com.jhh.jhs.loan.mapper.gen.LoanOrderDOMapper;
import com.jhh.jhs.loan.mapper.gen.domain.LoanOrderDO;
import com.jhh.jhs.loan.mapper.manager.BankListMapper;
import com.jhh.jhs.loan.service.capital.BasePayServiceImpl;
import com.jhh.jhs.loan.service.haier.HaierPayService;
import com.jhh.jhs.loan.service.haier.HaierRefundService;
import com.jhh.jhs.loan.service.haier.HaierWithholdService;
import com.jhh.jhs.loan.util.GetBank;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.JedisCluster;

/**
 * 2018/4/13.
 */
@Slf4j
public class TradeHaierServiceImpl extends BasePayServiceImpl implements TradePayService {

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private BankMapper bankMapper;

    @Autowired
    private LoanOrderDOMapper loanOrderDOMapper;

    @Autowired
    private HaierPayService haierPayService;

    @Autowired
    private HaierWithholdService haierWithholdService;

    @Autowired
    private HaierCallBackService haierCallBackService;
    @Autowired
    private HaierRefundService haierRefundService;

    @Autowired
    private BankListMapper bankListMapper;

    @Autowired
    private JedisCluster jedisCluster;

    @Override
    public ResponseDo<String> postPayment(TradeVo tradeVo) throws Exception {
        Person p = personMapper.selectByPrimaryKey(tradeVo.getPerId());
        Bank bank = bankMapper.selectByBankNumAndStatus(tradeVo.getBankNum());
        LoanOrderDO loanOrderDO = loanOrderDOMapper.selectBySerNo(tradeVo.getSerialNo());
        //修改订单channel 用于区分渠道
        updateOrder(loanOrderDO);
        //组装请求参数
        HaierPayVo vo = new HaierPayVo();
        vo.setOut_trade_no(tradeVo.getSerialNo());
        vo.setBank_account_name(p.getName());
        vo.setBank_card_no(bank.getBankNum());
        vo.setBank_code(GetBank.getname(bank.getBankId()));
        vo.setBank_name("工商银行");//随便填的
        vo.setAmount(String.valueOf(tradeVo.getAmount()));
        vo.setPayer_identity(HaierConstants.PAYEE_IDENTITY);
        vo.setBiz_product_code(HaierConstants.BIZ_PRODUCT_CODE_PAY);
        vo.setPay_product_code(HaierConstants.PAY_PRODUCT_CODE_PAY);
        vo.setNotify_url(HaierConstants.PAY_NOTIFY_URL);
        NoteResult noteResult = haierPayService.partnerBankPaying(vo);
        log.info("请求海尔response:" + noteResult);
        if (noteResult == null) {
            return ResponseDo.newFailedDo("支付异常");
        }
        return verifyHaierResponse(noteResult, loanOrderDO);
    }

    @Override
    public ResponseDo<String> postDeduct(TradeVo tradeVo) throws Exception {
        Person p = personMapper.selectByPrimaryKey(tradeVo.getPerId());
        Bank bank = bankMapper.selectByBankNumAndStatus(tradeVo.getBankNum());
        LoanOrderDO loanOrderDO = loanOrderDOMapper.selectBySerNo(tradeVo.getSerialNo());
        //修改订单channel 用于区分渠道
        updateOrder(loanOrderDO);
        HaierDeductVo vo = new HaierDeductVo();
        vo.setOut_trade_no(tradeVo.getSerialNo());
        vo.setBank_account_name(p.getName());
        vo.setCertificates_type(HaierConstants.CERTIFICATES_TYPE);
        vo.setCertificates_number(p.getCardNum());
        vo.setBank_card_no(bank.getBankNum());
        vo.setBank_code(GetBank.getname(bank.getBankId()));
        vo.setPayable_amount(String.valueOf(tradeVo.getAmount()));
        vo.setPayee_identity(HaierConstants.PAYEE_IDENTITY);
        vo.setBiz_product_code(HaierConstants.BIZ_PRODUCT_CODE);
        vo.setPay_product_code(HaierConstants.PAY_PRODUCT_CODE);
        vo.setNotify_url(HaierConstants.DEDUCT_NOTIFY_URL);
        // withhold.setPayable_amount(Double.parseDouble("1")); //TODO:测试用的
        NoteResult noteResult = haierWithholdService.partnerBankWithholding(vo);
        log.info("请求海尔response:" + noteResult);
        if (noteResult == null) {
            return ResponseDo.newFailedDo("支付异常");
        }
        return verifyHaierResponse(noteResult, loanOrderDO);
    }

    @Override
    public ResponseDo<?> state(String serialNo) {
        log.info("\n[ serNO" + serialNo);
        ResponseDo result = new ResponseDo<>(204,"该笔订单已经处理完成，请返回查看");
        LoanOrderDO loanOrderDO = loanOrderDOMapper.selectBySerNo(serialNo);
        if ("p".equals(loanOrderDO.getRlState())) {
            //加入redis锁 防止重复提交
            if (!"OK".equals(jedisCluster.set(RedisConst.PAY_ORDER_KEY + serialNo, "off", "NX", "EX", 3 * 60))) {
                return new ResponseDo<>(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode(), "当前有还款在处理中，请稍后");
            }

            if (!"OK".equals(jedisCluster.set(RedisConst.PAY_REFUND_KEY + serialNo, "off", "NX", "EX", 3 * 60))) {
                return new ResponseDo<>(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode(), "当前有退款在处理中，请稍后");
            }

            try {
                NoteResult noteResult = haierCallBackService.orderStatus(serialNo);
                if ("00".equals(noteResult.getCode())) {
                    result.setCode(Constants.DeductQueryResponseConstants.SUCCESS_CODE);
                    result.setInfo("交易成功");
                } else if ("10".equals(noteResult.getCode())) {
                    result.setCode(Constants.DeductQueryResponseConstants.PROGRESSING);
                    result.setInfo(noteResult.getInfo());
                } else if ("20".equals(noteResult.getCode())) {
                    result.setCode(Constants.DeductQueryResponseConstants.SUCCESS_ORDER_SETTLE_FAIL);
                    result.setInfo(noteResult.getInfo());
                }
            } catch (Exception e) {
                log.error("出错：", e);
                return new ResponseDo<>(204, "处理中,请稍候");
            } finally {
                jedisCluster.del(RedisConst.PAY_ORDER_KEY + serialNo);
                jedisCluster.del(RedisConst.PAY_REFUND_KEY + serialNo);
            }
        }

        return result;
    }

    @Override
    public ResponseDo<?> batchDeduct(TradeBatchVo vo) {
        return null;
    }

    private ResponseDo<String> verifyHaierResponse(NoteResult noteResult, LoanOrderDO loanOrder) throws Exception {
        ResponseDo<String> result = new ResponseDo<>();
        if ("200".equals(noteResult.getCode())) {
            result.setCode(AgentDeductResponseEnum.SUCCESS_CODE.getCode());
            result.setInfo(AgentDeductResponseEnum.SUCCESS_CODE.getMsg());
            result.setData(loanOrder.getSerialNo());
            log.info("代扣最终返回的参数：" + JSONObject.toJSONString(noteResult));
            return result;
        } else if ("202".equals(noteResult.getCode())) {
            log.info("代扣最终返回的参数：" + JSONObject.toJSONString(noteResult));
            result.setCode(200);
            result.setInfo(noteResult.getInfo());
            return result;
        } else {
            handleFail(loanOrder, noteResult.getInfo());
            log.info("代扣最终返回的参数：" + JSONObject.toJSONString(noteResult));
            result.setCode(201);
            result.setInfo(noteResult.getInfo());
            return result;
        }
    }

    /**
     *  修改订单channel 用于区分渠道
     * @param loanOrderDO
     */
    private void updateOrder(LoanOrderDO loanOrderDO){
        //修改订单类型
        loanOrderDO.setPayChannel(PayChannelEnum.YSB.getCode());
        loanOrderDO.setChannel("haier");
        loanOrderDOMapper.updateByPrimaryKeySelective(loanOrderDO);
    }

   /* private String queryBankCode(int bankId){
        BankList list = bankListMapper.selectByPrimaryKey(bankId);
        return list.getBankCode();
    }*/


    @Override
    public ResponseDo<String> refund(TradeVo tradeVo) throws Exception {

        Person p = personMapper.selectByPrimaryKey(tradeVo.getPerId());
        Bank bank = bankMapper.selectByBankNumEffective(tradeVo.getBankNum(),tradeVo.getPerId());
        LoanOrderDO loanOrderDO = loanOrderDOMapper.selectBySerNo(tradeVo.getSerialNo());
        //修改订单channel 用于区分渠道
        updateOrder(loanOrderDO);

        //组装请求参数
        HaierRefundVo vo = new HaierRefundVo();
        vo.setOut_trade_no(tradeVo.getSerialNo());
        vo.setBank_account_name(p.getName());
        vo.setBank_card_no(bank.getBankNum());
        vo.setBank_code(GetBank.getname(bank.getBankId()));
        vo.setBank_name("工商银行");//随便填的
        vo.setAmount(String.valueOf(tradeVo.getAmount()));
        vo.setPayer_identity(HaierConstants.PAYEE_IDENTITY);
        vo.setBiz_product_code(HaierConstants.BIZ_PRODUCT_CODE_PAY);
        vo.setPay_product_code(HaierConstants.PAY_PRODUCT_CODE_PAY);
        vo.setNotify_url(HaierConstants.PAY_NOTIFY_URL);
        NoteResult noteResult = haierRefundService.partnerBankRefund(vo);
        log.info("请求海尔response:" + noteResult);
        if (ObjectUtils.isEmpty(noteResult)) {
            return ResponseDo.newFailedDo("支付异常");
        }
        return verifyHaierResponse(noteResult, loanOrderDO);

    }
}
