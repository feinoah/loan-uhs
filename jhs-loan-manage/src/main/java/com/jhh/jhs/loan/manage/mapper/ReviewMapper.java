package com.jhh.jhs.loan.manage.mapper;

import com.jhh.jhs.loan.entity.manager.Review;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface ReviewMapper extends Mapper<Review> {

	List manualAuditReport(Map map);

	List getauditsforUser(Map map);

	List getManuallyReview(Map map);

}