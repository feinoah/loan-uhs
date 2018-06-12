package com.jhh.jhs.loan.api.message;


import com.jhh.jhs.loan.api.entity.ResponseDo;

/**
 *
 */
public interface MessageService {

    /**发送站内信通用接口
     * @param userId
     * @param templateId
     * @param params
     * @return
     */
    ResponseDo<String> setMessage(String userId, String templateId, String params);
}
