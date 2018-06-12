package com.jhh.jhs.loan.service;

import com.jhh.jhs.loan.model.Response;

public interface RobotService {

    void sendDataToBaikelu();
    /**
     * 发送风控订单，用于首单审核
     * @param borrId 合同Id
     * @return
     */
    Response sendRcOrder(Integer borrId) throws Exception;
}
