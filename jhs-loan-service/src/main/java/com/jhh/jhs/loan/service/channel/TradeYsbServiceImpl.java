package com.jhh.jhs.loan.service.channel;

import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.channel.TradePayService;
import com.jhh.jhs.loan.api.constant.Constants;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.AgentDeductBatchRequest;
import com.jhh.jhs.loan.api.entity.capital.TradeBatchVo;
import com.jhh.jhs.loan.api.entity.capital.TradeVo;
import com.jhh.jhs.loan.common.enums.AgentDeductResponseEnum;
import com.jhh.jhs.loan.common.enums.PayChannelEnum;
import com.jhh.jhs.loan.common.payment.PaymentUtil;
import com.jhh.jhs.loan.common.util.PropertiesReaderUtil;
import com.jhh.jhs.loan.common.util.RedisConst;
import com.jhh.jhs.loan.entity.app.Bank;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.mapper.app.BankMapper;
import com.jhh.jhs.loan.mapper.app.PersonMapper;
import com.jhh.jhs.loan.mapper.gen.LoanOrderDOMapper;
import com.jhh.jhs.loan.mapper.gen.domain.LoanOrderDO;
import com.jhh.jhs.loan.service.capital.BasePayServiceImpl;
import com.jhh.jhs.loan.service.capital.CollectUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.JedisCluster;

import java.io.UnsupportedEncodingException;

/**
 * 银生宝代付代扣操作
 */
@Slf4j
public class TradeYsbServiceImpl extends BasePayServiceImpl implements TradePayService {

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private LoanOrderDOMapper loanOrderDOMapper;

    @Autowired
    private BankMapper bankMapper;

    @Value("${isTest}")
    private String isTest;

    @Value("${collectResponseUrl}")
    String responseUrl;

    @Autowired
    private JedisCluster jedisCluster;

    @Override
    public ResponseDo<String> postPayment(TradeVo vo) throws Exception {
        Person p = personMapper.selectByPrimaryKey(vo.getPerId());
        LoanOrderDO loanOrder = loanOrderDOMapper.selectBySerNo(vo.getSerialNo());
        //修改订单部分信息
        updateOrder(loanOrder);
        String ll = postYsbPay(p, loanOrder);
        JSONObject obj1 = JSONObject.parseObject(ll);
        String result_code = obj1.getString("result_code");
        String result_msg = obj1.getString("result_msg");

        log.info("[代付] 请求返回体 result_code {},result_msg{}", result_code, result_msg);
        if ("on".equals(isTest)) {
            result_code = "0000";
            result_msg = "测试默认通过";
        }
        if ("0000".equals(result_code)) {
            log.info("\n [代付] 直接调用银生宝 返回正在处理中,完美");
            return ResponseDo.newSuccessDo();
        } else {
            handleFail(loanOrder, result_msg);
            return ResponseDo.newFailedDo(result_msg);
        }
    }

    @Override
    public ResponseDo<String> postDeduct(TradeVo vo) {
        String response;
        Person p = personMapper.selectByPrimaryKey(vo.getPerId());
        Bank bank = bankMapper.selectByBankNumAndStatus(vo.getBankNum());
        LoanOrderDO loanOrder = loanOrderDOMapper.selectBySerNo(vo.getSerialNo());
        updateOrder(loanOrder);
        try {
            response = CollectUtils.requestCollect(String.valueOf(vo.getAmount()), p.getPhone(), bank.getSubContractNum(), vo.getSerialNo(),
                    responseUrl);
            if (null == response) {
                log.error("支付渠道异常，请稍候再试");
                return ResponseDo.newFailedDo("支付渠道异常，请稍候再试");
            }
            log.info("银生宝代扣返回参数 response = " + response);
            return verifyYsbResponse(loanOrder, response);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("支付渠道异常，请稍候再试", e);
            return ResponseDo.newFailedDo("支付渠道异常，请稍候再试");
        }
    }

    @Override
    public ResponseDo<?> state(String serialNo) {
        log.info("[查询开始] -->走银生宝直连");
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
                String response;
                if ("1".equals(loanOrderDO.getType())) {
                    response = PaymentUtil.queryOrderStatus(serialNo);
                } else {
                    response = CollectUtils.queryOrder(serialNo);
                }
                YsbOrderStatusResponse orderResponse = JSONObject.parseObject(response, YsbOrderStatusResponse.class);
                if (orderResponse != null) {
                    this.afterState(orderResponse, result);
                }
            } catch (Exception e) {
                log.error("出错：", e);
                result.setCode(Constants.DeductQueryResponseConstants.PROGRESSING);
                result.setInfo("交易处理中");
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

    @Override
    public ResponseDo<String> refund(TradeVo tradeVo) throws Exception {
        log.info("银生宝开始退款服务，请求参数 tradeVo = {}",tradeVo);
        Person p = personMapper.selectByPrimaryKey(tradeVo.getPerId());
        LoanOrderDO loanOrder = loanOrderDOMapper.selectBySerNo(tradeVo.getSerialNo());
        //修改订单部分信息
        updateOrder(loanOrder);
        String ll = postYsbPay(p, loanOrder);
        JSONObject obj1 = JSONObject.parseObject(ll);
        String result_code = obj1.getString("result_code");
        String result_msg = obj1.getString("result_msg");

        log.info("[代付] 请求返回体 result_code {},result_msg {}", result_code, result_msg);
        if ("on".equals(isTest)) {
            result_code = "0000";
            result_msg = "测试默认通过";
        }
        if ("0000".equals(result_code)) {
            log.info("\n [代付] [退款] 直接调用银生宝 返回正在处理中,完美");
            return ResponseDo.newSuccessDo();
        } else {
            handleFail(loanOrder, result_msg);
            return ResponseDo.newFailedDo(result_msg);
        }
    }

    /**
     * 验证银生宝返回参数并修改流水
     *
     * @param response 验证参数
     * @return 返回结果
     */
    private ResponseDo<String> verifyYsbResponse(LoanOrderDO loanOrder, String response) throws Exception {
        ResponseDo<String> result = ResponseDo.newSuccessDo();
        JSONObject res = JSONObject.parseObject(response);
        String result_code = res.getString("result_code");
        String result_msg = res.getString("result_msg");
        result.setInfo(result_msg);
        //保存失败的原因
        if (!"0000".equals(result_code)) {
            //第三方受理失败
            handleFail(loanOrder, result.getInfo());
            log.info("代扣最终返回的参数：" + JSONObject.toJSONString(result));
            return ResponseDo.newFailedDo(result.getInfo());
        }
        return result;
    }


    private String postYsbPay(Person p, LoanOrderDO loanOrder) throws UnsupportedEncodingException {
        String name = p.getName();
        String cardNo = p.getBankCard();
        String orderId = loanOrder.getSerialNo();
        String purpose = "放款";
        String amount = String.valueOf(loanOrder.getActAmount());
        String responseUrl = PropertiesReaderUtil.read("third", "payContCallBackUrl");

        return PaymentUtil.payCont(name, cardNo, orderId, purpose, amount, responseUrl);
    }

    private ResponseDo<?> afterState(YsbOrderStatusResponse response, ResponseDo result) throws Exception {
        //处理中
        if (("on").equals(isTest)) {
            result.setCode(Constants.DeductQueryResponseConstants.SUCCESS_CODE);
            result.setInfo("查询成功");
            return result;
        }
        if ("0000".equals(response.getResult_code())) {
            if ("00".equals(response.getStatus())) {
                result.setCode(Constants.DeductQueryResponseConstants.SUCCESS_CODE);
                result.setInfo("查询成功");
            } else if ("10".equals(response.getStatus())) {
                result.setCode(Constants.DeductQueryResponseConstants.PROGRESSING);
                result.setInfo("交易处理中");
                //暂无
            } else if ("20".equals(response.getStatus())) {
                result.setCode(Constants.DeductQueryResponseConstants.SUCCESS_ORDER_SETTLE_FAIL);
                result.setInfo(response.getDesc() == null ? response.getResult_msg() : response.getDesc());
            }
        } else {
            result.setCode(Constants.DeductQueryResponseConstants.SUCCESS_ORDER_SETTLE_FAIL);
            result.setInfo(response.getDesc() == null ? response.getResult_msg() : response.getDesc());
        }
        return result;
    }

    /**
     * 修改订单channel 用于区分渠道
     *
     * @param loanOrderDO
     */
    private void updateOrder(LoanOrderDO loanOrderDO) {
        //修改订单类型
        loanOrderDO.setPayChannel(PayChannelEnum.YSB.getCode());
        loanOrderDO.setChannel("ysb");
        loanOrderDOMapper.updateByPrimaryKeySelective(loanOrderDO);
    }

    @Data
    private static class YsbOrderStatusResponse {
        private String result_code;
        private String result_msg;
        private String desc;
        private String status;
        private String amount;
    }

}
