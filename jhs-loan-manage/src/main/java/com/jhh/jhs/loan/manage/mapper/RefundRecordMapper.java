package com.jhh.jhs.loan.manage.mapper;

import com.jhh.jhs.loan.entity.refund.RefundReview;
import com.jhh.jhs.loan.entity.refund.RefundReviewVo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface RefundRecordMapper extends Mapper<RefundReview> {
    /**
     * 退款流水
     * @returnF
     */
    List getRefundRecord();

}