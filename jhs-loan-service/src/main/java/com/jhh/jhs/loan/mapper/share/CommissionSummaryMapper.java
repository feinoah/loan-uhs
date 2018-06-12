package com.jhh.jhs.loan.mapper.share;

import com.jhh.jhs.loan.entity.share.CommissionSummary;

public interface CommissionSummaryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CommissionSummary record);

    int insertSelective(CommissionSummary record);

    CommissionSummary selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CommissionSummary record);

    int updateByPrimaryKey(CommissionSummary record);

    CommissionSummary queryCommissionByPersonId(String personId);
}