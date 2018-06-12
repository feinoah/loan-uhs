package com.jhh.jhs.loan.dao;

import com.jhh.jhs.loan.entity.share.CommissionSummary;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author xingmin
 */
public interface CommissionSummaryMapper extends Mapper<CommissionSummary>{

    /**
     * 佣金汇总
     * @return
     */
    int updateCommissionSummary();
}
