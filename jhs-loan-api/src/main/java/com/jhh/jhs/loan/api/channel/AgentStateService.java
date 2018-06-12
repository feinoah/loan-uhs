package com.jhh.jhs.loan.api.channel;

import com.jhh.jhs.loan.api.entity.ResponseDo;
import java.util.List;

/**
 * 2018/3/30.
 */
public interface AgentStateService {

    /**
     * 查询订单的支付状态
     * @param serNO
     * @return ResponseDo
     */
    ResponseDo state(String serNO) throws Exception;

}
