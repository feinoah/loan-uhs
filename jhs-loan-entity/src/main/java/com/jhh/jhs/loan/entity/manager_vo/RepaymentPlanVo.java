package com.jhh.jhs.loan.entity.manager_vo;

import java.io.Serializable;

import com.jhh.jhs.loan.entity.manager.RepaymentPlan;

public class RepaymentPlanVo extends RepaymentPlan implements Serializable{
	private String borrNum;

	/**
	 * @return the borrNum
	 */
	public String getBorrNum() {
		return borrNum;
	}

	/**
	 * @param borrNum the borrNum to set
	 */
	public void setBorrNum(String borrNum) {
		this.borrNum = borrNum;
	}
	
   
}