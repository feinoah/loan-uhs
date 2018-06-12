package com.jhh.jhs.loan.manage.controller.risk.management;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jhh.jhs.loan.common.util.DateUtil;
import com.jhh.jhs.loan.common.util.Detect;
import com.jhh.jhs.loan.common.util.ExcelUtils;
import com.jhh.jhs.loan.entity.manager.AuditsUser;
import com.jhh.jhs.loan.entity.manager.ManuallyReview;
import com.jhh.jhs.loan.manage.entity.Response;
import com.jhh.jhs.loan.manage.service.risk.RiskManageService;
import com.jhh.jhs.loan.manage.utils.QueryParamUtils;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.vo.NormalExcelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


@Controller
@RequestMapping("/risk")
public class RiskManageController {

	private static final Logger logger = LoggerFactory.getLogger(RiskManageController.class);

	@Autowired
    RiskManageService riskManageService;

	/**
	 * 风险控制，待审核(审核管理)
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/auditsforUser", method = RequestMethod.GET)
	public String getauditsforUser(HttpServletRequest request) {
		QueryParamUtils.buildPage(request);
		List result = riskManageService.auditsforUser(request.getParameterMap());
		return JSON.toJSONString(new PageInfo(result));
	}

	/**
	 * 审核管理导出
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/auditsforUser/export", method = RequestMethod.POST)
	public void exportAuditsforUser(HttpServletRequest request, HttpServletResponse response) {
		QueryParamUtils.buildPage(request,100000);
		List result = riskManageService.auditsforUser(request.getParameterMap());

		Map<String, Object> map = new HashMap();
		map.put(NormalExcelConstants.FILE_NAME, DateUtil.getDateTimeString(Calendar.getInstance().getTime()));
		map.put(NormalExcelConstants.CLASS, AuditsUser.class);
		map.put(NormalExcelConstants.DATA_LIST, result);
		map.put(NormalExcelConstants.PARAMS, new ExportParams());
		ExcelUtils.jeecgSingleExcel(map, request,response);
	}

	/**
	 * 风险控制，人工审核（人工审核结果）
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/manuallyReview", method = RequestMethod.GET)
	public String getManuallyReview(HttpServletRequest request) {
		int offset =  Integer.valueOf(request.getParameter("skip") == null ? "0" : request.getParameter("skip"));
		int size = Integer.valueOf(request.getParameter("take") == null ? Integer.MAX_VALUE + "" : request.getParameter("take"));

		List result = riskManageService.getManuallyReview(request.getParameterMap(), offset, size);
		return JSON.toJSONString(new PageInfo(result));
	}

	/**
	 * 人工审核结果导出
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/manuallyReview/export", method = RequestMethod.POST)
	public void export(HttpServletRequest request, HttpServletResponse response) {
		List result = riskManageService.getManuallyReview(request.getParameterMap(),0,100000);

		Map<String, Object> map = new HashMap();
		map.put(NormalExcelConstants.FILE_NAME, DateUtil.getDateTimeString(Calendar.getInstance().getTime()));
		map.put(NormalExcelConstants.CLASS, ManuallyReview.class);
		map.put(NormalExcelConstants.DATA_LIST, result);
		map.put(NormalExcelConstants.PARAMS, new ExportParams());
		ExcelUtils.jeecgSingleExcel(map, request,response);
	}

	/**
	 * 查询风控审核人员
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/reviewers", method = RequestMethod.GET)
	public List getReviewers(String status) {
		return riskManageService.getReviewers(status);
	}
	/**
	 * 修改风控审核人员状态
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/reviewers/status", method = RequestMethod.POST)
	public Response modefiyReviewersStatus(String brroIds, String status) {
		return riskManageService.modefiyReviewersStatus(brroIds, status);
	}

}

