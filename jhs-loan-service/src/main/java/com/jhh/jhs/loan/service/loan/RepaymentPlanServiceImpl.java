package com.jhh.jhs.loan.service.loan;

import com.alibaba.dubbo.config.annotation.Service;
import com.jhh.jhs.loan.api.loan.RepaymentPlanService;
import com.jhh.jhs.loan.entity.loan.RepaymentTermPlan;
import com.jhh.jhs.loan.entity.manager.RepaymentPlan;
import com.jhh.jhs.loan.mapper.manager.RepaymentPlanMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 还款计划具体实现
 *
 * @author xingmin
 */
@Service
public class RepaymentPlanServiceImpl implements RepaymentPlanService {

    @Autowired
    private RepaymentPlanMapper repaymentPlanMapper;

    @Override
    public List getRepaymentTermPlan(String borrowId) {
        return repaymentPlanMapper.getRepaymentTermPlan(borrowId);
    }

    @Override
    public List<RepaymentPlan> getOverdueRepaymentPlan(String borrId) {
        return repaymentPlanMapper.getOverdueRepaymentPlan(borrId);
    }
}
