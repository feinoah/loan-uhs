package com.jhh.jhs.loan.mapper.loan;

import com.jhh.jhs.loan.entity.loan.PerAccountLog;

import java.util.List;

public interface PerAccountLogMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PerAccountLog record);

    int insertSelective(PerAccountLog record);

    PerAccountLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PerAccountLog record);

    int updateByPrimaryKey(PerAccountLog record);
    
    List<PerAccountLog> getPerAccountLog(String userId, int start, int pageSize);
}