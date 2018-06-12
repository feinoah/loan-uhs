package com.jhh.jhs.loan.mapper.app;

import com.jhh.jhs.loan.entity.manager.CodeValue;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 2017/12/29.
 */
public interface CodeValueMapper {


    List<CodeValue> getCodeValueAll(@Param("codeType") String codeType);

    //根据type和code查询meaning
    String getMeaningByTypeCode(@Param("code_type") String code_type, @Param("code_code") String code_code);

    List<String> getMeaning(@Param("code_type") String code_type, @Param("code_code") String code_code);

    /**
     * get所有可用的codeValues
     * @param codeType
     * @return codeValues的集合
     */
    List<CodeValue>  getEnabledCodeValues(@Param("codeType") String codeType);
    //查询百可录首单审核主动打电话限制时间
    String selectBaikeLuDate(String date);

    CodeValue selectByCodeType(@Param("codeType") String codeType);
}
