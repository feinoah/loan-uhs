package com.jhh.jhs.loan.mapper.loan;

import com.jhh.jhs.loan.entity.loan.CompanyBank;

public interface CompanyBankMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CompanyBank record);

    int insertSelective(CompanyBank record);

    CompanyBank selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CompanyBank record);

    int updateByPrimaryKeyWithBLOBs(CompanyBank record);

    int updateByPrimaryKey(CompanyBank record);
}