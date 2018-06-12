package com.jhh.jhs.loan.manage.mapper;


import com.jhh.jhs.loan.entity.manager.SmsTemplate;

import java.util.List;

public interface SmsTemplateMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SmsTemplate record);

    SmsTemplate selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SmsTemplate record);

    int updateByPrimaryKey(SmsTemplate record);
    
    List<SmsTemplate> getAllSmsTemplateList();
}