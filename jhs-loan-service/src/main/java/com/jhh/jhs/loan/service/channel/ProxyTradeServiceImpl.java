package com.jhh.jhs.loan.service.channel;

import com.alibaba.dubbo.config.annotation.Service;
import com.jhh.jhs.loan.api.channel.TradeFactory;
import com.jhh.jhs.loan.api.channel.TradePayService;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.TradeBatchVo;
import com.jhh.jhs.loan.api.entity.capital.TradeVo;
import com.jhh.jhs.loan.common.enums.PayTriggerStyleEnum;
import com.jhh.jhs.loan.constant.Constant;
import com.jhh.jhs.loan.entity.manager.CodeValue;
import com.jhh.jhs.loan.entity.manager.Order;
import com.jhh.jhs.loan.mapper.app.CodeValueMapper;
import com.jhh.jhs.loan.mapper.manager.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.JedisCluster;

/**
 * 代理查询 所选渠道 和业务无关
 */
@Slf4j
@Service
public class ProxyTradeServiceImpl extends ProxyBaseServiceImpl implements TradePayService {

    @Autowired
    private TradeFactory factory;

    @Autowired
    private CodeValueMapper codeValueMapper;

    @Autowired
    private OrderMapper orderMapper;


    @Override
    public ResponseDo<String> postPayment(TradeVo vo) {
        try {
            //查询渠道开关
            CodeValue codeValue = selectByCodeType(Constant.PAY_SWITCH);
            String payChannel;
            if (StringUtils.isEmpty(vo.getPayChannel())) {
                vo.setPayChannel(codeValue.getMeaning());
                payChannel = choosePayChannel(codeValue);
            } else {
                payChannel = choosePayChannel(vo.getPayChannel());
            }
            if (StringUtils.isEmpty(payChannel)) {
                return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
            }
            return factory.createBean(payChannel).postPayment(vo);
        } catch (Exception e) {
            log.error("渠道选择异常", e);
            return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
        }
    }

    @Override
    public ResponseDo<String> postDeduct(TradeVo vo) {
        try {
            CodeValue codeValue = selectByCodeType(userTrigger(vo.getTriggerStyle()) ? Constant.REPAY_SWITCH : Constant.DEDUCT_SWITCH);
            String payChannel;
            if (StringUtils.isEmpty(vo.getPayChannel())) {
                vo.setPayChannel(codeValue.getMeaning());
                payChannel = choosePayChannel(codeValue);
            } else {
                payChannel = choosePayChannel(vo.getPayChannel());
            }
            if (StringUtils.isEmpty(payChannel)) {
                return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
            }
            return factory.createBean(payChannel).postDeduct(vo);
        } catch (Exception e) {
            log.error("渠道选择异常", e);
            return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
        }
    }

    @Override
    public ResponseDo<?> state(String serialNo) {
        Order order = orderMapper.selectBySerial(serialNo);
        if (order != null) {
            if (ysbState(order.getType())) {
                return factory.createBean(Constant.YSB_TRADE_CODE).state(serialNo);
            } else if (haierState(order.getType())) {
                return factory.createBean(Constant.HAIER_TRADE_CODE).state(serialNo);
            } else if (payCenterState(order.getType())) {
                return factory.createBean(Constant.PAYCENTER_TRADE_CODE).state(serialNo);
            } else if (commissionDrawalState(order.getType())) {
                String channel = commissionDrawChannel(order.getChannel());
                return factory.createBean(channel).state(serialNo);
            } else {
                return ResponseDo.newFailedDo("订单类型错误");
            }
        } else {
            return ResponseDo.newFailedDo("订单不存在");
        }
    }

    @Override
    public ResponseDo<?> batchDeduct(TradeBatchVo vo) {
        try {
            CodeValue codeValue = selectByCodeType(Constant.DEDUCT_SWITCH);
            String payChannel;
            if (StringUtils.isEmpty(vo.getPayChannel())) {
                vo.setPayChannel(codeValue.getMeaning());
                payChannel = choosePayChannel(codeValue);
            } else {
                payChannel = choosePayChannel(vo.getPayChannel());
            }
            return factory.createBean(payChannel).batchDeduct(vo);
        } catch (Exception e) {
            log.error("渠道选择异常", e);
            return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
        }
    }

    @Override
    public ResponseDo<String> refund(TradeVo tradeVo) throws Exception {
        try {
            //查询渠道开关
            CodeValue codeValue = selectByCodeType(Constant.REFUND_SWITCH);
            String payChannel;
            if (StringUtils.isEmpty(tradeVo.getPayChannel())) {
                tradeVo.setPayChannel(codeValue.getMeaning());
                payChannel = choosePayChannel(codeValue);
            } else {
                payChannel = choosePayChannel(tradeVo.getPayChannel());
            }
            if (StringUtils.isEmpty(payChannel)) {
                return ResponseDo.newFailedDo("渠道发生异常，请联系管理员");
            }
            return factory.createBean(payChannel).refund(tradeVo);
        } catch (Exception e) {
            log.error("渠道选择异常，异常信息 {} ", e);
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

    private String choosePayChannel(CodeValue codeValue) {
        if (Constant.PAYCENTER_CHANNEL_TYPE.equals(codeValue.getCodeCode())) {
            return Constant.PAYCENTER_TRADE_CODE;
        } else if (Constant.LOCAL_CHANNEL_TYPE.equals(codeValue.getCodeCode())) {
            return "trade" + codeValue.getMeaning();
        } else {
            return null;
        }
    }

    private String choosePayChannel(String payChannel) {
        if (Constant.YSB_CHANNEL.equals(payChannel) || Constant.HAIER_CHANNEL.equals(payChannel)){
            return "trade" + payChannel;
        }else {
            return Constant.PAYCENTER_TRADE_CODE;
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

}
