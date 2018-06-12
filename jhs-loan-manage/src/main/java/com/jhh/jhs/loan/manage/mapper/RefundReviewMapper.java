package com.jhh.jhs.loan.manage.mapper;

import com.jhh.jhs.loan.entity.refund.RefundReview;
import com.jhh.jhs.loan.entity.refund.RefundReviewVo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface RefundReviewMapper extends Mapper<RefundReview> {
    /**
     * 退款列表
     * @param map
     * @returnF
     */
    List<RefundReviewVo> queryRefundReviewList(Map map);

    /**
     * 条数
     */
    Integer queryRefundReviewListCount(Map map);

    /**
     * 根据订单号和用户Id查询审核记录
     * @param orderId
     * @param perId
     * @return
     */
    RefundReview selectByOrderIdAndPerId(@Param("orderId") String orderId, @Param("perId") Integer perId);
}