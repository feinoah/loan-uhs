package com.jhh.jhs.loan.mapper.manager;

import com.jhh.jhs.loan.entity.manager.CodeType;

import java.util.List;

public interface CodeTypeMapper {
	List<CodeType> getCodeTypeList();
	
    int deleteByPrimaryKey(Integer id);

    int insert(CodeType record);

    int insertSelective(CodeType record);

    CodeType selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CodeType record);

    int updateByPrimaryKey(CodeType record);
}