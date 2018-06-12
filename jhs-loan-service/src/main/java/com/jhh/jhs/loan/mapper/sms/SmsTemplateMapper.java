package com.jhh.jhs.loan.mapper.sms;

import com.jhh.jhs.loan.entity.manager.SmsTemplate;
import org.apache.ibatis.annotations.Param;

/**
 * 短信模板
 */
public interface SmsTemplateMapper {

    /**
     *  获取对应短信模板
     * @param templateSeq
     * @return
     */
    public SmsTemplate getSmsTemplate(@Param("templateSeq") int templateSeq);
}
