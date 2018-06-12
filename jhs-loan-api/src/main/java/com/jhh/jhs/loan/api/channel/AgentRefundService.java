package com.jhh.jhs.loan.api.channel;


import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.AgentRefundRequest;

/**
 * 退款服务
 */
public interface AgentRefundService {

    /**
     * 退款
     */
    ResponseDo<?> refund(AgentRefundRequest refund);

}
