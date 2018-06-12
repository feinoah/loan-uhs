package com.jhh.jhs.loan.manage.mapper;

import com.jhh.jhs.loan.entity.manager_vo.CommissionReceiveVo;
import com.jhh.jhs.loan.entity.share.CommissionReview;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface CommissionReviewMapper extends Mapper<CommissionReview> {
    /**
     *
     * @param personId
     */
    List<CommissionReceiveVo> getConmmissionReceiveHistoryByPersonId(String personId);
}