package com.jhh.jhs.loan.api.channel;

import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.AgentPayRequest;

/**
 * 2018/3/30.
 */
public interface AgentPayService {
    
    ResponseDo<?> pay(AgentPayRequest pay);
}
