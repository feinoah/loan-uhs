package com.jhh.jhs.loan.api.channel;

import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.AgentDeductBatchRequest;
import com.jhh.jhs.loan.api.entity.capital.AgentDeductRequest;

/**
 *  代扣
 */
public interface AgentDeductService {
    
     ResponseDo<?> deduct(AgentDeductRequest request);

     ResponseDo<?> deductBatch(AgentDeductBatchRequest requests);


}
