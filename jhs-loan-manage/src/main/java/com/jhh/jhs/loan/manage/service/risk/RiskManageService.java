package com.jhh.jhs.loan.manage.service.risk;

import com.jhh.jhs.loan.manage.entity.Response;

import java.util.List;
import java.util.Map;

public interface RiskManageService {
	/**
	 * 风险控制，待审核(审核管理)
	 * @param parameterMap
	 * @return
	 */
	List auditsforUser(Map<String, String[]> parameterMap);

	/**
	 * 风险控制，人工审核（人工审核结果）
	 * @param parameterMap
	 * @param offset
	 * @param size
	 * @return
	 */
	List getManuallyReview(Map<String, String[]> parameterMap,Integer offset, Integer size );

	/**
	 * 查询风控审核人员
	 * @return
	 */
	List getReviewers(String status);

	/**
	 * 修改风控审核人员状态
	 * @param brroIds
	 * @param status
	 * @return
	 */
	Response modefiyReviewersStatus(String brroIds, String status);
}
