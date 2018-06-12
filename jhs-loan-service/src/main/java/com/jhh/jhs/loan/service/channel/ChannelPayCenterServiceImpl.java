package com.jhh.jhs.loan.service.channel;

import com.google.common.collect.Lists;
import com.jhh.jhs.loan.api.channel.AgentBatchStateService;
import com.jhh.jhs.loan.api.channel.AgentChannelService;
import com.jhh.jhs.loan.api.channel.TradeBatchStateService;
import com.jhh.jhs.loan.api.channel.TradePayService;
import com.jhh.jhs.loan.api.constant.Constants;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.AgentDeductBatchRequest;
import com.jhh.jhs.loan.api.entity.capital.AgentDeductRequest;
import com.jhh.jhs.loan.api.entity.capital.AgentPayRequest;
import com.jhh.jhs.loan.api.entity.capital.AgentRefundRequest;
import com.jhh.jhs.loan.api.entity.capital.TradeBatchVo;
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
import com.jhh.jhs.loan.entity.callback.LKLBatchCallback;
import com.jhh.jhs.loan.mapper.app.BankMapper;
import com.jhh.jhs.loan.mapper.app.BorrowListMapper;
import com.jhh.jhs.loan.mapper.app.CodeValueMapper;
import com.jhh.jhs.loan.mapper.app.PersonMapper;
import com.jhh.jhs.loan.mapper.gen.LoanOrderDOMapper;
import com.jhh.jhs.loan.mapper.gen.domain.LoanOrderDO;
import com.jhh.jhs.loan.service.capital.BasePayServiceImpl;
import com.jhh.pay.driver.pojo.PayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 2018/3/30.
 */
@Slf4j
public class ChannelPayCenterServiceImpl extends BasePayServiceImpl implements AgentChannelService, AgentBatchStateService {

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private CodeValueMapper codeValueMapper;

    @Autowired
    private BankMapper bankMapper;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private LoanOrderDOMapper loanOrderDOMapper;

    @Resource(name = "tradePayService")
    private TradePayService tradeLocalService;

    @Value("${isTest}")
    private String isTest;

    @Value("${batchDeductSize}")
    private String batchDeductSize;

    @Autowired
    private TradeBatchStateService tradeBatchStateService;

    @Override
    public ResponseDo<?> pay(AgentPayRequest pay) {
        log.info("\n[代付开始] -->走支付中心渠道 pay" + pay);

        // 幂等性操作 防止重复放款
        ResponseDo responseDo = formLock(pay.getBorrId());
        if (CodeReturn.success != responseDo.getCode()) {
            return responseDo;
        }
        try {
            if (checkAgentPayLog(pay.getBorrId(), pay.getUserId())) {
                // 获取合同信息并更改合同状态
                PersonInfoDto dto = getPersonInfo(pay.getBorrId());
                // 合同状态改为放款中
                dto.getBorrowList().setBorrStatus(CodeReturn.STATUS_COM_PAYING);
                dto.getBorrowList().setUpdateDate(new Date());
                borrowListMapper.updateByPrimaryKeySelective(dto.getBorrowList());
                // 生成流水号
                LoanOrderDO loanOrder = savePayLoanOrder(dto, Constants.payOrderType.PAYCENTER_PAY_TYPE, pay.getTriggerStyle(), Constants.PayStyleConstants.PAY_JHH_YSB_CODE_VALUE);
                //生成手续费订单
                //手续费
                String fee = codeValueMapper.getMeaningByTypeCode("payment_fee", "2");
                saveFeeOrder(loanOrder, fee);
                Bank bank = bankMapper.selectPrimayCardByPerId(String.valueOf(pay.getUserId()));
                //发起代付
                TradeVo vo = new TradeVo(pay.getUserId(), loanOrder.getSerialNo(),
                        pay.getPayChannel(), pay.getTriggerStyle(), bank.getBankNum(), loanOrder.getActAmount().floatValue());
                return tradeLocalService.postPayment(vo);
            } else {
                log.info("\n[代付] 此order已经有一笔单子在处理中");
                return ResponseDo.newFailedDo(AgentpayResultEnum.HAD_PROCESSING.getDesc());
            }
        } catch (Exception e) {
            log.error("支付中心代付出现异常", e);
            return ResponseDo.newFailedDo("系统繁忙");
        } finally {
            jedisCluster.del(RedisConst.PAYCONT_KEY + pay.getBorrId());
        }
    }

    @Override
    public ResponseDo state(String serNO) throws Exception {
        log.info("-------------支付中心查询订单号 " + serNO);
        ResponseDo<?> state = tradeLocalService.state(serNO);
        if (state != null) {
            afterStateHandle(state, serNO);
            return state;
        } else {
            return ResponseDo.newFailedDo("查询失败，请稍候再试");
        }
    }

    @Override
    public ResponseDo batchState(List<String> loanOrder) throws Exception {
        log.info("-------------支付中心查询订单号 loanOrder = " + loanOrder.toString());
        //将list切割
        List<List<String>> partition = Lists.partition(loanOrder, Integer.parseInt(batchDeductSize));
        if (partition.size() < 1) {
            return ResponseDo.newSuccessDo();
        }
        partition.forEach(v -> {
            //防止重复提交
            lock(v);
            if (v.size() < 1) {
                return;
            }
            try {
                ResponseDo<PayResponse> result = tradeBatchStateService.batchState(v);
                if (!(result != null && Constants.CommonPayResponseConstants.SUCCESS_CODE == result.getCode())) {
                    return;
                }
                PayResponse resp = result.getData();
                if (Constants.PayStyleConstants.JHH_PAY_STATE_PROGRESSING.equals(resp.getState())
                        || Constants.PayStyleConstants.JHH_PAY_STATE_SUCCESS.equals(resp.getState())) {
                    resp.getSimpleOrders().forEach(f -> {
                        LoanOrderDO loanOrderDO = loanOrderDOMapper.selectBySerNo(f.getOrderNo());
                        if (StringUtils.isEmpty(loanOrderDO.getSid()) || StringUtils.isEmpty(loanOrderDO.getChannel())) {
                            loanOrderDO.setSid(f.getSid());
                            loanOrderDO.setChannel(f.getChannelKey());
                            loanOrderDOMapper.updateByPrimaryKeySelective(loanOrderDO);
                        }
                        if (Constants.PayStyleConstants.JHH_PAY_STATE_ERROR.equals(f.getState())
                                || Constants.PayStyleConstants.JHH_PAY_STATE_FAIL.equals(f.getState())) {
                            handleFail(loanOrderDO, f.getMsg());
                        } else if (Constants.PayStyleConstants.JHH_PAY_STATE_SUCCESS.equals(f.getState())) {
                            handleSuccess(loanOrderDO);
                        }
                    });
                }
            } catch (Exception e) {
                log.error("", e);
            } finally {
                unlock(v);
            }
        });
        return null;
    }

    /**
     * 清除锁
     *
     * @param serialNos
     */
    private void unlock(List<String> serialNos) {
        serialNos.forEach(v -> jedisCluster.del(RedisConst.PAY_ORDER_KEY + v));
    }

    /**
     * 上锁 如存在 则提剔除
     *
     * @param serialNos
     */
    private void lock(List<String> serialNos) {
        Iterator<String> iterator = serialNos.iterator();
        while (iterator.hasNext()) {
            if (!"OK".equals(jedisCluster.set(RedisConst.PAY_ORDER_KEY + iterator.next(), "off", "NX", "EX", 3 * 60))) {
                iterator.remove();
            }
        }
    }

    @Override
    public void batchCallback(LKLBatchCallback batchCallback) {
        //处理部分失败订单
        if (batchCallback != null && batchCallback.getExtension() != null && batchCallback.getExtension().get("exceptionMaps") != null) {
            Map<String,String> failOrder = (Map<String, String>) batchCallback.getExtension().get("exceptionMaps");
            log.info("批量代扣回调解析参数\n"+failOrder);
            failOrder.forEach((k, v) -> {
                LoanOrderDO loanOrderDO = loanOrderDOMapper.selectBySerNo(k);
                updateOrderForFail(loanOrderDO, v);
            });
        }
    }

    @Override
    public ResponseDo<?> deduct(AgentDeductRequest request) {
        log.info("[代扣开始] -->走支付中心渠道{} ", request);
        ResponseDo<String> result;
        //TODO:下一版本要改掉
        if (!StringUtils.isEmpty(request.getBorrNum())) {
            request.setBorrId(request.getBorrNum());
        }
        //请结算锁
        result = settleLock(request.getTriggerStyle());
        if (AgentDeductResponseEnum.SUCCESS_CODE.getCode() != result.getCode()) {
            return result;
        }
        //加入redis锁 防止重复提交
        if (!"OK".equals(jedisCluster.set(RedisConst.PAY_ORDER_KEY + request.getBorrId(), "off", "NX", "EX", 3 * 60))) {
            return new ResponseDo<>(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode(), "当前有还款在处理中，请稍后");
        }
        try {
            ResponseDo<TradeVo> deductResult = doDeduct(request, "deduct");
            if (Constants.CommonPayResponseConstants.SUCCESS_CODE == deductResult.getCode()) {
                result =  tradeLocalService.postDeduct(deductResult.getData());
                return result;
            } else {
                return deductResult;
            }
        } catch (Exception e) {
            log.error("出错：" + e.getMessage(), e);
            result.setCode(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode());
            result.setInfo(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getMsg());
            return result;
        } finally {
            jedisCluster.del(RedisConst.PAY_ORDER_KEY + request.getBorrId());
        }
    }


    @Override
    public ResponseDo<?> deductBatch(AgentDeductBatchRequest requests) {
        log.info("[批量代扣开始] -->走支付中心渠道{} ", requests);
        if (requests == null) {
            return ResponseDo.newFailedDo("未获取需要批量的记录");
        }
        //清结算锁
        ResponseDo<?> result = settleLock(requests.getTriggerStyle());
        if (AgentDeductResponseEnum.SUCCESS_CODE.getCode() != result.getCode()) {
            return result;
        }
        //循环保存订单
        List<TradeVo> deduct = new ArrayList<>();
        for (AgentDeductRequest request : requests.getRequests()) {
            //加入redis锁 防止重复提交
            if (!"OK".equals(jedisCluster.set(RedisConst.PAY_ORDER_KEY + request.getBorrId(), "off", "NX", "EX", 3 * 60))) {
                log.error("批量代扣订单中用正在处理的订单 borrNum = " + request.getBorrId());
                continue;
            }
            try {
                ResponseDo<TradeVo> deductBatch = doDeduct(request, "deductBatch");
                if (Constants.CommonPayResponseConstants.SUCCESS_CODE == deductBatch.getCode()) {
                    deduct.add(deductBatch.getData());
                }
            } catch (Exception e) {
                log.error("出错：" + e.getMessage(), e);
                result.setCode(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode());
                result.setInfo(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getMsg());
            } finally {
                jedisCluster.del(RedisConst.PAY_ORDER_KEY + request.getBorrId());
            }
        }
        if (deduct.size() > 0) {
            TradeBatchVo tradeBatchVo = new TradeBatchVo(deduct, deduct.size(), requests.getPayChannel(), requests.getOptPerson());
            return tradeLocalService.batchDeduct(tradeBatchVo);
        } else {
            return ResponseDo.newFailedDo("没有可执行的订单");
        }
    }

    /**
     * @param request
     * @param deductType
     * @return
     */
    private ResponseDo<TradeVo> doDeduct(AgentDeductRequest request, String deductType) {

        BorrowList borrow = borrowListMapper.getBorrowListByBorrId(Integer.parseInt(request.getBorrId()));
        //更新代扣时间
        borrow.setCurrentRepayTime(new Date());
        borrowListMapper.updateByPrimaryKeySelective(borrow);
        //查询用户
        Person person = personMapper.selectByPrimaryKey(borrow.getPerId());
        //修改request参数
        ResponseDo<?> responseDo = updateAgentDeductRequest(request, borrow, person);
        if (!AgentDeductResponseEnum.SUCCESS_CODE.getCode().equals(responseDo.getCode())) {
            return ResponseDo.newFailedDo(responseDo.getInfo());
        }
        //提前结清，正常结算这俩中类型做金额判断
        NoteResult canPay = canPayCollect(borrow, Double.parseDouble(request.getOptAmount()), request.getType());
        if (!CodeReturn.SUCCESS_CODE.equals(canPay.getCode())) {
            return ResponseDo.newFailedDo(canPay.getInfo());
        }
        String type = "deduct".equals(deductType) ? setTypeAndSerialNo(request) : Constants.payOrderType.PAYCENTER_DEDUCTBATCH_TYPE;
        LoanOrderDO loanOrder = saveDeductLoanOrder(request, person.getId(), borrow.getId(), type, Constants.PayStyleConstants.PAY_JHH_YSB_CODE_VALUE);
        //从快速编码表查出手续费  1：代收
        String fee = codeValueMapper.getMeaningByTypeCode("payment_fee", "1");
        //修改 在第三方受理成功之前，生成手续费订单
        saveFeeOrder(loanOrder, fee);
        //发起代扣请求，请求统一支付中心
        Float finalAmount = (new BigDecimal(request.getOptAmount()).add(new BigDecimal(fee))).floatValue();
        //发起代扣
        TradeVo vo = new TradeVo(person.getId(), loanOrder.getSerialNo(), request.getPayChannel(),
                Integer.parseInt(request.getTriggerStyle()), request.getBankNum(), finalAmount, request.getValidateCode());
        return ResponseDo.newSuccessDo(vo);
    }


    private String setTypeAndSerialNo(AgentDeductRequest request) {
        String type;
        if (PayTriggerStyleEnum.USER_TRIGGER.getCode().toString().equals(request.getTriggerStyle())) {
            type = Constants.payOrderType.PAYCENTER_DEDUCT_TYPE;
        } else {
            type = Constants.payOrderType.PAYCENTER_INITIATIVE_TYPE;
        }
        return type;
    }

    @Override
    public ResponseDo<?> refund(AgentRefundRequest refund) {
        //加入redis锁 防止重复提交
        if (!"OK".equals(jedisCluster.set(RedisConst.PAY_REFUND_KEY + refund.getPerId(), "off", "NX", "EX", 3 * 60))) {
            return new ResponseDo<>(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode(), "当前有还款在处理中，请稍后");
        }

//        if(verifyLoanOrderStatus(refund.getPerId(),Constants.payOrderType.PAYCENTER_REFUND_PAY_TYPE)){
//            return ResponseDo.newFailedDo("存在未完成的退款，请先稍等");
//        }

        //获取银行卡id
        Bank bank = bankMapper.selectByBankNumEffective(refund.getBankNum(), refund.getPerId());
        if (bank == null || !bank.getPerId().equals(refund.getPerId())) {
            return ResponseDo.newFailedDo("该银行卡不存在，请验证");
        }

        //生成订单
        LoanOrderDO loanOrder = savePayLoanOrder(refund, bank.getId(), Constants.payOrderType.PAYCENTER_REFUND_PAY_TYPE, Constants.PayStyleConstants.PAY_JHH_YSB_CODE_VALUE);
        String fee = codeValueMapper.getMeaningByTypeCode("payment_fee", "5");
        saveFeeOrder(loanOrder, fee);

        //发起付款
        TradeVo tradeVo = new TradeVo(refund.getPerId(), loanOrder.getSerialNo(), refund.getPayChannel(),
                refund.getTriggerStyle(), bank.getBankNum(), refund.getAmount().floatValue());
        ResponseDo<String> result = new ResponseDo<>();
        try {
            ResponseDo<String> responseDo = tradeLocalService.refund(tradeVo);
            log.info("支付中心退款返回结果 responseDo = {}", responseDo);
            result.setCode(responseDo.getCode());
            result.setInfo(responseDo.getInfo());
            result.setData(loanOrder.getSerialNo());
            return result;
        } catch (Exception e) {
            log.error("出错：" + e.getMessage(), e);
            result.setCode(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getCode());
            result.setInfo(AgentDeductResponseEnum.BUSINESS_INNER_PARAM_ERROR.getMsg());
            return result;
        } finally {
            jedisCluster.del(RedisConst.PAY_REFUND_KEY + refund.getPerId());
        }
    }

}
