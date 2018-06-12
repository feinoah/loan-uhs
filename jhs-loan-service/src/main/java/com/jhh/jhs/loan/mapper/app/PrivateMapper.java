package com.jhh.jhs.loan.mapper.app;

import com.jhh.jhs.loan.entity.app.Private;

public interface PrivateMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Private record);

    int insertSelective(Private record);

    Private selectByPrimaryKey(Integer id);
    
    Private selectByPerId(Integer per_id);

    int updateByPrimaryKeySelective(Private record);

    int updateByPrimaryKey(Private record);
}