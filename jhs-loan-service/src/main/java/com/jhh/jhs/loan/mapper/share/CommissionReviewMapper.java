package com.jhh.jhs.loan.mapper.share;

import com.jhh.jhs.loan.entity.share.CommissionReview;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CommissionReviewMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CommissionReview record);

    int insertSelective(CommissionReview record);

    CommissionReview selectByPrimaryKey(Integer id);

    List<CommissionReview> selectByCondition(@Param("perId") String personId, @Param("status") List<String> status);

    int updateByPrimaryKeySelective(CommissionReview record);

    int updateByPrimaryKey(CommissionReview record);
}