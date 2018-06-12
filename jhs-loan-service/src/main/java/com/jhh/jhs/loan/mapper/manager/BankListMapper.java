package com.jhh.jhs.loan.mapper.manager;

import com.jhh.jhs.loan.entity.manager.BankList;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BankListMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(BankList record);

    int insertSelective(BankList record);

    BankList selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(BankList record);

    int updateByPrimaryKey(BankList record);
    
    List<BankList> selectBySupport(String support);

    BankList selectByBankCode(@Param("bankCode") String bankCode);
}