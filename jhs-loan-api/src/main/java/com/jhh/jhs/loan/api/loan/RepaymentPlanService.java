package com.jhh.jhs.loan.api.loan;

import com.jhh.jhs.loan.entity.manager.RepaymentPlan;

import java.util.List;

/**
 * Created by chenchao on 2018/1/10.
 */
public interface RepaymentPlanService {

    public List getRepaymentTermPlan(String borrowId);

    List<RepaymentPlan> getOverdueRepaymentPlan(String borrId);
}
