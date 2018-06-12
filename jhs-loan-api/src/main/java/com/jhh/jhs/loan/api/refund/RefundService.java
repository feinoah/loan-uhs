package com.jhh.jhs.loan.api.refund;

import com.jhh.jhs.loan.api.entity.ResponseDo;

public interface RefundService {
    /**
     * 退款更新订单流水
     *
     * @param serialNo 订单号
     */
    ResponseDo<?> refundState(String serialNo);

}
