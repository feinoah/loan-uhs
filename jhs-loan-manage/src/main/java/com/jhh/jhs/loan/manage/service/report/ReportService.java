package com.jhh.jhs.loan.manage.service.report;

import com.jhh.jhs.loan.manage.entity.Response;

import java.io.IOException;

public interface ReportService {

	/**
	 * 获取聚信立报告页面
	 * @return
	 */
	Response getPolyXinliReport();

	/**
	 * 获取通讯录
	 * @param perId
	 * @param phones
	 * @param offset
	 * @param size
	 * @return
	 */
	Response getContact(Integer perId,String phones, Integer offset,Integer size) throws IOException;

	/**
	 * 导出通讯录信息查询
	 * @param perId
	 * @return
	 */
	Response getContactForExport(Integer perId);
}
