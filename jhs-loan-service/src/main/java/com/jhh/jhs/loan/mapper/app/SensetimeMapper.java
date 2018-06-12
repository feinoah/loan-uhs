package com.jhh.jhs.loan.mapper.app;

import com.jhh.jhs.loan.entity.app.Sensetime;

public interface SensetimeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Sensetime record);

    int insertSelective(Sensetime record);

    Sensetime selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Sensetime record);

    int updateByPrimaryKey(Sensetime record);
    
    int selectTimes(String per_id);
}