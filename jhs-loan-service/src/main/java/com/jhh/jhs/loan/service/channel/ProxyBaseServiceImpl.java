package com.jhh.jhs.loan.service.channel;

import com.jhh.jhs.loan.api.constant.Constants;
import com.jhh.jhs.loan.constant.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * 2018/4/17.
 */
 class ProxyBaseServiceImpl {

   boolean ysbState(String type) {
        List<String> list = new ArrayList<>();
        list.add(Constants.payOrderType.YSB_DEDUCT_TYPE);
        list.add(Constants.payOrderType.YSB_DEDUCTBATCH_TYPE);
        list.add(Constants.payOrderType.YSB_PAY_TYPE);
        list.add(Constants.payOrderType.YSB_INITIATIVE_TYPE);
        list.add(Constants.payOrderType.YSB_REFUND_PAY_TYPE);
        return list.contains(type);
    }

    boolean haierState(String type) {
        List<String> list = new ArrayList<>();
        list.add(Constants.payOrderType.HAIER_DEDUCT_TYPE);
        list.add(Constants.payOrderType.HAIER_DEDUCTBATCH_TYPE);
        list.add(Constants.payOrderType.HAIER_PAY_TYPE);
        list.add(Constants.payOrderType.HAIER_INITIATIVE_TYPE);
        list.add(Constants.payOrderType.HAIER_REFUND_PAY_TYPE);
        return list.contains(type);
    }

     boolean payCenterState(String type) {
        List<String> list = new ArrayList<>();
        list.add(Constants.payOrderType.PAYCENTER_DEDUCT_TYPE);
        list.add(Constants.payOrderType.PAYCENTER_DEDUCTBATCH_TYPE);
        list.add(Constants.payOrderType.PAYCENTER_PAY_TYPE);
        list.add(Constants.payOrderType.PAYCENTER_INITIATIVE_TYPE);
        list.add(Constants.payOrderType.PAYCENTER_REFUND_PAY_TYPE);
        return list.contains(type);
    }

    boolean commissionDrawalState(String type){
        return Constants.payOrderType.COMMISSION_WITHDRAWAL_TYPE.equals(type);
    }

    String commissionDrawChannel(String channel) {
        if (Constants.payOrderType.COMMISSION_YSBCHANNEL_TYPE.equals(channel)){
            return Constant.YSB_TRADE_CODE;
        }else if (Constants.payOrderType.COMMISSION_HAIERCHANNEL_TYPE.equals(channel)){
            return Constant.HAIER_TRADE_CODE;
        }else {
            return Constant.PAYCENTER_TRADE_CODE;
        }
    }


}
