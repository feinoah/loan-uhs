package com.jhh.jhs.loan.api.sms;

import com.jhh.jhs.loan.api.entity.ResponseDo;

/**
 *
 */
public interface SmsService {

    /**
     *  发送带参数的短信
     * @param templateSeq 模板ID
     * @param smsTemplateArgs 短信参数
     * @param phone  手机号
     * @return  结果
     */
    public ResponseDo sendSms(int templateSeq, String phone, String... smsTemplateArgs);

    /**
     *  发送短信
     * @param templateSeq 模板id
     * @param phone 手机号
     * @return 结果
     */
    public ResponseDo sendSms(int templateSeq, String phone);

    /**
     *吴迅催收短信
     * @param templateSeq
     * @param phone
     * @param smsTemplateArgs
     * @return
     */
    public ResponseDo overdueSms(int templateSeq,String phone,String... smsTemplateArgs);
}
