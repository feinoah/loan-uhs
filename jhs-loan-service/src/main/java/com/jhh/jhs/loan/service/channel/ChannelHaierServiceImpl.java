package com.jhh.jhs.loan.service.channel;

import com.jhh.jhs.loan.api.channel.AgentChannelService;
import com.jhh.jhs.loan.api.channel.TradePayService;
import com.jhh.jhs.loan.api.constant.Constants;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.AgentDeductBatchRequest;
import com.jhh.jhs.loan.api.entity.capital.AgentDeductRequest;
import com.jhh.jhs.loan.api.entity.capital.AgentPayRequest;
import com.jhh.jhs.loan.api.entity.capital.AgentRefundRequest;
import com.jhh.jhs.loan.api.entity.capital.TradeVo;
import com.jhh.jhs.loan.common.enums.AgentDeductResponseEnum;
import com.jhh.jhs.loan.common.enums.AgentpayResultEnum;
import com.jhh.jhs.loan.common.enums.PayTriggerStyleEnum;
import com.jhh.jhs.loan.common.util.CodeReturn;
import com.jhh.jhs.loan.common.util.RedisConst;
import com.jhh.jhs.loan.entity.PersonInfoDto;
import com.jhh.jhs.loan.entity.app.Bank;
import com.jhh.jhs.loan.entity.app.BorrowList;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.mapper.app.BankMapper;
import com.jhh.jhs.loan.mapper.app.BorrowListMapper;
import com.jhh.jhs.loan.mapper.app.CodeValueMapper;
import com.jhh.jhs.loan.mapper.app.PersonMapper;
import com.jhh.jhs.loan.mapper.gen.domain.LoanOrderDO;
import com.jhh.jhs.loan.service.capital.BasePayServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 海尔支付渠道
 */
@Slf4j
public class ChannelHaierServiceImpl extends BasePayServiceImpl implements AgentChannelService {

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private CodeValueMapper codeValueMapper;

    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private JedisCluster jedisCluster;

    @Resource(name = "tradePayService")
    private TradePayService tradeService;

    @Autowired
    private BankMapper bankMapper;

    @Value("${isTest}")
    private String isTest;

    @Value("${collectResponseUrl}")
    String responseUrl;

    @Override
    public ResponseDo<?> pay(AgentPayRequest pay) {

        log.info("\n[代付开始] -->走直连海尔代付通道 pay" + pay);
        // 幂等性操作 防止重复放款
        ResponseDo responseDo = formLock(pay.getBorrId());
        if (CodeReturn.success != responseDo.getCode()) {
            return responseDo;
        }
        try {
            if (checkAgentPayLog(pay.getBorrId(), pay.getUserId())) {
                // 获取用户相关信息
                PersonInfoDto dto = getPersonInfo(pay.getBorrId());
                //只要状态为放款失败或者为已签约的合同才能放款
                if (CodeReturn.STATUS_COM_PAY_FAIL.equals(dto.getBorrowList().getBorrStatus())
                        || CodeReturn.STATUS_SIGNED.equals(dto.getBorrowList().getBorrStatus())) {
                    // 合同状态改为放款中
                    dto.getBorrowList().setBorrStatus(CodeReturn.STATUS_COM_PAYING);
                    dto.getBorrowList().setUpdateDate(new Date());
                    borrowListMapper.updateByPrimaryKeySelective(dto.getBorrowList());
                    // 生成流水号
                    LoanOrderDO loanOrder = savePayLoanOrder(dto, Constants.payOrderType.HAIER_PAY_TYPE,
                            pay.getTriggerStyle(), Constants.PayStyleConstants.PAY_YSB_CODE_VALUE);
                    // 手续费订单
                    // 算手续费...暂定每笔2块钱手续费，5万封顶
                    String fee = codeValueMapper.getMeaningByTypeCode("payment_fee", "2");
                    saveFeeOrder(loanOrder, fee);
                    Bank pBank = bankMapper.selectPrimayCardByPerId(String.valueOf(pay.getUserId()));
                    if (pBank == null) {
                        log.info("\n [代付] 无法获取用户主卡，per_id = {}", pay.getUserId());
                        return ResponseDo.newFailedDo(AgentpayResultEnum.BANKCARD_ERROR.getDesc());
                    }
                    //调用海尔
                    //------------------------------------------------------------------------------
                    TradeVo tradeVo = new TradeVo(pay.getUserId(), loanOrder.getSerialNo(), pay.getPayChannel(), pay.getTriggerStyle(),
                            pBank.getBankNum(), loanOrder.getActAmount().floatValue());
                    return tradeService.postPayment(tradeVo);
                } else {
                    log.info("[代付] 合同状态异常");
                    return ResponseDo.newFailedDo(AgentpayResultEnum.BORROW_EXCEPTION.getDesc());
                }
            } else {
                log.info("\n[代付] 此order已经有一笔单子在处理中");
                return ResponseDo.newFailedDo(AgentpayResultEnum.HAD_PROCESSING.getDesc());
            }

        } catch (Exception e) {
            log.error("悠多多系统出现错误1", e);
            return ResponseDo.newFailedDo("系统异常");
        } finally {
            jedisCluster.del(RedisConst.PAYCONT_KEY + pay.getBorrId());
        }
    }

    @Override
    public ResponseDo state(String serNO) throws Exception {
        log.info("-------------海尔直连查询订单号 "+serNO);
        ResponseDo<?> state = tradeService.state(serNO);
        if (state != null){
            afterStateHandle(state,serNO);
            return state;
        }else {
            return ResponseDo.newFailedDo("查询失败，请稍候再试");
        }
    }

    @Override
    public ResponseDo<?> deduct(AgentDeductRequest request) {
        log.info("海尔直连代扣请求参数:{}", request);
        //TODO:下一版本要改掉
        if (!StringUtils.isEmpty(request.getBorrNum())) {
            request.setBorrId(request.getBorrNum());
        }
        ResponseDo result;
        //清结算锁
        result = settleLock(request.getTriggerStyle());
        if (AgentDeductResponseEnum.SUCCESS_CODE.getCode() != result.getCode()) {
            return result;
        }
        BorrowList borrow = borrowListMapper.getBorrowListByBorrId(Integer.parseInt(request.getBorrId()));
        //更新代扣时间
        borrow.setCurrentRepayTime(new Date());
        borrowListMapper.updateByPrimaryKeySelective(borrow);
        //查找用户
        Person person = personMapper.selectByPrimaryKey(borrow.getPerId());
        //修改request参数
        result = updateAgentDeductRequest(request, borrow, person);
        if (!AgentDeductResponseEnum.SUCCESS_CODE.getCode().equals(result.getCode())) {
            return result;
        }
        //提前结清，正常结算这俩中类型做金额判断
        NoteResult canPay = canPayCollect(borrow, Double.parseDouble(request.getOptAmount()), request.getType());
        if (CodeReturn.FAIL_CODE.equals(canPay.getCode())) {
            result.setCode(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode());
            result.setInfo(canPay.getInfo());
            return result;
        }
        //加入redis锁 防止重复提交
        if (!"OK".equals(jedisCluster.set(RedisConst.PAY_ORDER_KEY + request.getBorrId(), "off", "NX", "EX", 5 * 60))) {
            return new ResponseDo(Constants.CommonPayResponseConstants.BUSINESS_ERROR_CODE, "当前有还款在处理中，请稍后");
        }
        //保存订单
        try {
            String type = setTypeAndSerialNo(request);
            LoanOrderDO loanOrderDO = saveDeductLoanOrder(request, person.getId(), borrow.getId(), type, Constants.PayStyleConstants.PAY_YSB_CODE_VALUE);
            //保存手续费订单
            String fee = codeValueMapper.getMeaningByTypeCode("payment_fee", "1");
            saveFeeOrder(loanOrderDO, fee);
            Float finalAmount = (new BigDecimal(request.getOptAmount()).add(new BigDecimal(fee))).floatValue();
            TradeVo tradeVo = new TradeVo(person.getId(), loanOrderDO.getSerialNo(), request.getPayChannel(), Integer.parseInt(request.getTriggerStyle()),
                    request.getBankNum(), finalAmount);
            return tradeService.postDeduct(tradeVo);
        } catch (Exception e) {
            log.error("代扣出现异常", e);
            return ResponseDo.newFailedDo("系统繁忙");
        } finally {
            jedisCluster.del(RedisConst.PAY_ORDER_KEY + request.getBorrId());
        }
    }

    @Override
    public ResponseDo<?> deductBatch(AgentDeductBatchRequest requests) {
        //暂时默认返回成功//todo
        log.info("海尔直连批量代扣方法 requests" + requests);
        ResponseDo result = new ResponseDo(200, "成功");

        return result;
    }


    private String setTypeAndSerialNo(AgentDeductRequest request) {
        String type;
        if (PayTriggerStyleEnum.USER_TRIGGER.getCode().toString().equals(request.getTriggerStyle())) {
            type = Constants.payOrderType.HAIER_DEDUCT_TYPE;
        }  else {
            type = Constants.payOrderType.HAIER_INITIATIVE_TYPE;
        }
        return type;
    }


    @Override
    public ResponseDo<?> refund(AgentRefundRequest refund) {
        //加入redis锁 防止重复提交
        if (!"OK".equals(jedisCluster.set(RedisConst.PAY_REFUND_KEY + refund.getPerId(), "off", "NX", "EX", 3 * 60))) {
            return new ResponseDo<>(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode(), "当前有还款在处理中，请稍后");
        }

        if(verifyLoanOrderStatus(refund.getPerId(),Constants.payOrderType.HAIER_REFUND_PAY_TYPE)){
            return ResponseDo.newFailedDo("存在未完成的退款，请先稍等");
        }

        //获取银行卡id
        Bank bank = bankMapper.selectByBankNumEffective(refund.getBankNum(),refund.getPerId());
        if (bank == null || !bank.getPerId().equals(refund.getPerId())) {
            return ResponseDo.newFailedDo("该银行卡不存在，请验证");
        }

        //生成订单
        LoanOrderDO loanOrder = savePayLoanOrder(refund, bank.getId(), Constants.payOrderType.HAIER_REFUND_PAY_TYPE,Constants.PayStyleConstants.PAY_YSB_CODE_VALUE);
        String fee = codeValueMapper.getMeaningByTypeCode("payment_fee", "5");
        saveFeeOrder(loanOrder, fee);

        //发起付款
        TradeVo tradeVo = new TradeVo(refund.getPerId(), loanOrder.getSerialNo(), refund.getPayChannel(),
                refund.getTriggerStyle(), bank.getBankNum(), refund.getAmount().floatValue());
        try {
            ResponseDo<String> responseDo = tradeService.refund(tradeVo);
            log.info("海尔退款服务返回结果 responseDo = {}",responseDo);

            responseDo.setData(loanOrder.getSerialNo());
            return responseDo;
        } catch (Exception e) {
            log.error("悠多多系统出现错误1", e);
            return ResponseDo.newFailedDo("系统异常");
        } finally {
            jedisCluster.del(RedisConst.PAY_REFUND_KEY + refund.getPerId());
        }
    }
}
