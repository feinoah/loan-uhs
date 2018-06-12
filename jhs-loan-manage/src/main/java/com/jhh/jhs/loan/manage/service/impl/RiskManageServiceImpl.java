package com.jhh.jhs.loan.manage.service.impl;

import com.github.pagehelper.PageHelper;
import com.jhh.jhs.loan.common.util.DateUtil;
import com.jhh.jhs.loan.entity.app.Reviewers;
import com.jhh.jhs.loan.entity.common.ResponseCode;
import com.jhh.jhs.loan.manage.entity.Response;
import com.jhh.jhs.loan.manage.mapper.ReviewersMapper;
import com.jhh.jhs.loan.manage.service.loan.CollectorsService;
import com.jhh.jhs.loan.manage.utils.QueryParamUtils;
import com.jhh.jhs.loan.manage.mapper.ReviewMapper;
import com.jhh.jhs.loan.manage.service.risk.RiskManageService;
import com.jhh.jhs.loan.manage.utils.Detect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class RiskManageServiceImpl implements RiskManageService {

	@Autowired
	private ReviewMapper reviewMapper;
	@Autowired
	private ReviewersMapper reviewersMapper;
	@Autowired
	CollectorsService collectorsService;

	@Override
	public List auditsforUser(Map<String, String[]> parameterMap) {
		//拿取filter参数
		Map<String, Object> param = QueryParamUtils.getargs(parameterMap,"makeborrDate", "desc");
		if (Detect.notEmpty(parameterMap.get("employNum"))) {
			param.put("employNum", parameterMap.get("employNum")[0]);
		}else{
			if(!Detect.notEmpty(param.get("makeborrDate_start") + "")){
				//审核管理默认展示一周数据
				Calendar calendar = new GregorianCalendar();
				calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 7);
				param.put("makeborrDate_start", DateUtil.getDateString(calendar.getTime()));
				Calendar calendar_end = new GregorianCalendar();
				calendar_end.set(Calendar.DATE, calendar_end.get(Calendar.DATE) + 1);
				param.put("makeborrDate_end",DateUtil.getDateString(calendar_end.getTime()));
			}
		}

		return reviewMapper.getauditsforUser(param);
	}

	@Override
	public List getManuallyReview(Map<String, String[]> parameterMap, Integer offset, Integer size) {
		Map<String, Object> param = null;
		if(Detect.notEmpty(parameterMap.get("borrStatus")) && Detect.notEmpty(parameterMap.get("borrStatus")[0])){
			//人工审核默认通讯录排序
			param = QueryParamUtils.getargs(parameterMap,"contactNum", "desc");
			param.put("borrStatusValue", parameterMap.get("borrStatus")[0]);
		}else {

			//人工审核结果默认审核时间排序

			param = QueryParamUtils.getargs(parameterMap,"auditTime", "desc");
			if(!Detect.notEmpty(param.get("auditTime_start") + "")){
				//默认展示一周数据
				Calendar calendar = new GregorianCalendar();
				calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 7);
				param.put("auditTime_start", DateUtil.getDateString(calendar.getTime()));
				Calendar calendar_end = new GregorianCalendar();
				calendar_end.set(Calendar.DATE, calendar_end.get(Calendar.DATE) + 1);
				param.put("auditTime_end",DateUtil.getDateString(calendar_end.getTime()));
			}

		}

		if(Detect.notEmpty(parameterMap.get("employNum")) && Detect.notEmpty(parameterMap.get("employNum")[0])){
			//主管看所有
			if(!collectorsService.isManager(parameterMap.get("employNum")[0])) {
				param.put("employNum", parameterMap.get("employNum")[0]);
			}
		}

		//人工审核条件
		param.put("isManual", "1");
		//分页
		PageHelper.offsetPage(offset, size);

		return reviewMapper.getManuallyReview(param);
	}

	@Override
	public List getReviewers(String status) {
		Example example = new Example(Reviewers.class);
		if(Detect.notEmpty(status)){
			example.createCriteria().andEqualTo("status", status);
		}
		return reviewersMapper.selectByExample(example);
	}

	@Override
	public Response modefiyReviewersStatus(String brroIds, String status) {
		Response response = new Response().code(ResponseCode.FIAL).msg("操作失败");
		if(Detect.notEmpty(brroIds) && Detect.notEmpty(status)){
			//修改状态
			Reviewers reviewers = new Reviewers();
			reviewers.setStatus(status);
			//条件ids
			String[] ids  = brroIds.split(",");
			Example example = new Example(Reviewers.class);
			example.createCriteria().andIn("id",Arrays.asList(ids));
			reviewersMapper.updateByExampleSelective(reviewers, example);

			response.code(ResponseCode.SUCCESS).msg("操作成功");
		}
		return response;
	}

}
