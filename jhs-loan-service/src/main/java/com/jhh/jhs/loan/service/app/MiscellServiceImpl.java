package com.jhh.jhs.loan.service.app;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.app.MiscellService;
import com.jhh.jhs.loan.entity.app.YmFeedback;

@Service
public class MiscellServiceImpl implements MiscellService {

	public String getMessageByUserId(String userId) {
		JSONObject obj = new JSONObject();
		obj.put("code", 0);
		obj.put("info", "消息列表成功");
		obj.put("data", 258);
		return obj.toString();
	}

	public String getMyBorrowList(String userId) {
		JSONObject obj = new JSONObject();
		obj.put("code", 0);
		obj.put("info", "获取我的借款记录成功");
		obj.put("data", 258);
		return obj.toString();
	}

	public String commonProblem(String userId) {
		JSONObject obj = new JSONObject();
		obj.put("code", 0);
		obj.put("info", "获取我的借款记录成功");
		obj.put("data", 258);
		return obj.toString();
	}

	public String feedback(YmFeedback feed) {
		JSONObject obj = new JSONObject();
		obj.put("code", 0);
		obj.put("info", "获取我的借款记录成功");
		obj.put("data", 258);
		return obj.toString();
	}

	public String getPersonInfo(String userId) {
		JSONObject obj = new JSONObject();
		obj.put("code", 0);
		obj.put("info", "获取我的借款记录成功");
		obj.put("data", 258);
		return obj.toString();
	}
	
}
