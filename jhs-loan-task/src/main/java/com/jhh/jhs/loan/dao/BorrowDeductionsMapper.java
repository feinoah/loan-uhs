package com.jhh.jhs.loan.dao;

import com.jhh.jhs.loan.entity.loan.BorrowDeductions;
import tk.mybatis.mapper.common.Mapper;

public interface BorrowDeductionsMapper extends Mapper<BorrowDeductions> {
    BorrowDeductions selectByBorrId(Integer borrId);
}
