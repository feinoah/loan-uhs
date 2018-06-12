package com.jhh.jhs.loan.entity.manager_vo;

import java.io.Serializable;

import com.jhh.jhs.loan.entity.manager.MsgTemplate;

public class MsgTemplateVo extends MsgTemplate implements Serializable {
	private String employName;

	/**
	 * @return the employName
	 */
	public String getEmployName() {
		return employName;
	}

	/**
	 * @param employName the employName to set
	 */
	public void setEmployName(String employName) {
		this.employName = employName;
	}
}