package com.jhh.jhs.loan.api.channel;

import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.entity.callback.LKLBatchCallback;

import java.util.List;

/**
 * 2018/6/5.
 */
public interface AgentBatchStateService {

    /**
     * 批量查询订单的支付状态
     * @param serialNo
     * @return ResponseDo
     */
    ResponseDo batchState(List<String> serialNo) throws Exception;

    void batchCallback(LKLBatchCallback batchCallback);
}
