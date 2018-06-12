package com.jhh.jhs.loan.service.channel;

import com.alibaba.dubbo.config.annotation.Service;
import com.jhh.jhs.loan.api.channel.AgentChannelService;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.AgentDeductBatchRequest;
import com.jhh.jhs.loan.api.entity.capital.AgentDeductRequest;
import com.jhh.jhs.loan.api.entity.capital.AgentPayRequest;
import com.jhh.jhs.loan.api.entity.capital.AgentRefundRequest;
import com.jhh.jhs.loan.common.enums.PayTriggerStyleEnum;
import com.jhh.jhs.loan.constant.Constant;
import com.jhh.jhs.loan.entity.manager.CodeValue;
import com.jhh.jhs.loan.entity.manager.Order;
import com.jhh.jhs.loan.mapper.app.CodeValueMapper;
import com.jhh.jhs.loan.mapper.manager.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 代理渠道寻找对应支付渠道
 */
@Service(validation = "true")
@Slf4j
public class ProxyChannelServiceImpl extends ProxyBaseServiceImpl implements AgentChannelService {

    @Autowired
    private AgentChannelFactory factory;

    @Autowired
    private OrderMapper orderMapper;


    @Autowired
    private CodeValueMapper codeValueMapper;

    @Override
    public ResponseDo<?> pay(AgentPayRequest pay) {
        try {
            //查询渠道开关
            CodeValue codeValue = selectByCodeType(Constant.PAY_SWITCH);
            String payChannel;
            if (StringUtils.isEmpty(pay.getPayChannel())) {
                pay.setPayChannel(codeValue.getMeaning());
                payChannel = choosePayChannel(codeValue);
            } else {
                payChannel = Constant.PAYCENTER_CHANNEL;
            }

            if (StringUtils.isEmpty(payChannel)) {
                return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
            }
            return factory.create(payChannel).pay(pay);
        } catch (Exception e) {
            log.error("渠道选择异常", e);
            return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
        }
    }

    @Override
    public ResponseDo state(String serNO) throws Exception {
        Order order = orderMapper.selectBySerial(serNO);
        if (order != null) {
            if (ysbState(order.getType())) {
                return factory.create(Constant.YSB_CHANNEL).state(serNO);
            } else if (haierState(order.getType())) {
                return factory.create(Constant.HAIER_CHANNEL).state(serNO);
            } else if (payCenterState(order.getType())) {
                return factory.create(Constant.PAYCENTER_CHANNEL).state(serNO);
            } else {
                return ResponseDo.newFailedDo("订单类型错误");
            }
        } else {
            return ResponseDo.newFailedDo("订单不存在");
        }
    }

    @Override
    public ResponseDo<?> deduct(AgentDeductRequest request) {
        try {
            CodeValue codeValue = selectByCodeType(userTrigger(Integer.parseInt(request.getTriggerStyle())) ? Constant.REPAY_SWITCH : Constant.DEDUCT_SWITCH);
            String payChannel;
            if (StringUtils.isEmpty(request.getPayChannel())) {
                request.setPayChannel(codeValue.getMeaning());
                payChannel = choosePayChannel(codeValue);
            } else {
                payChannel = Constant.PAYCENTER_CHANNEL;
            }
            if (StringUtils.isEmpty(payChannel)) {
                return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
            }
            return factory.create(payChannel).deduct(request);
        } catch (Exception e) {
            log.error("渠道选择异常", e);
            return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
        }

    }

    @Override
    public ResponseDo<?> deductBatch(AgentDeductBatchRequest requests) {
        try {
            CodeValue codeValue = selectByCodeType(Constant.DEDUCT_SWITCH);
            String payChannel;
            if (StringUtils.isEmpty(requests.getPayChannel())) {
                requests.setPayChannel(codeValue.getMeaning());
                payChannel = choosePayChannel(codeValue);
            } else {
                payChannel = Constant.PAYCENTER_CHANNEL;
            }

            if (StringUtils.isEmpty(payChannel)) {
                return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
            }
            return factory.create(payChannel).deductBatch(requests);
        } catch (Exception e) {
            log.error("渠道选择异常", e);
            return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
        }
    }

    /**
     * 查询渠道信息
     *
     * @param type
     * @return
     */
    private CodeValue selectByCodeType(String type) {
        return codeValueMapper.selectByCodeType(type);
    }

    /**
     * 选择渠道
     *
     * @param codeValue
     * @return
     */
    private String choosePayChannel(CodeValue codeValue) {
        if (Constant.PAYCENTER_CHANNEL_TYPE.equals(codeValue.getCodeCode())) {
            return Constant.PAYCENTER_CHANNEL;
        } else if (Constant.LOCAL_CHANNEL_TYPE.equals(codeValue.getCodeCode())) {
            return codeValue.getMeaning();
        } else {
            return null;
        }
    }

    /**
     * 是否为主动还款
     *
     * @param triggerStyle 是否为主动还款
     * @return
     */
    private boolean userTrigger(int triggerStyle) {
        return PayTriggerStyleEnum.USER_TRIGGER.getCode() == triggerStyle;
    }

    @Override
    public ResponseDo<?> refund(AgentRefundRequest refund) {
        try {

            //查询渠道开关
            CodeValue codeValue = selectByCodeType(Constant.REFUND_SWITCH);
            String payChannel;
            if (StringUtils.isEmpty(refund.getPayChannel())) {
                refund.setPayChannel(codeValue.getMeaning());
                payChannel = choosePayChannel(codeValue);
            } else {
                payChannel = Constant.PAYCENTER_CHANNEL;
            }

            if (StringUtils.isEmpty(payChannel)) {
                return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
            }
            return factory.create(payChannel).refund(refund);
        } catch (Exception e) {
            log.error("渠道选择异常", e);
            return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
        }
    }
}
