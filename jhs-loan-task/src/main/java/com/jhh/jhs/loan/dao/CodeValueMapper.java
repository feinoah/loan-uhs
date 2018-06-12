package com.jhh.jhs.loan.dao;


import com.jhh.jhs.loan.entity.manager.CodeValue;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CodeValueMapper {

    List<CodeValue> getCodeValueListByCode(String code_type);
}