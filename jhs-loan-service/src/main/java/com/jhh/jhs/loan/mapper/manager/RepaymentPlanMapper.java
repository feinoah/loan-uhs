package com.jhh.jhs.loan.mapper.manager;


import com.jhh.jhs.loan.entity.loan.RepaymentTermPlan;
import com.jhh.jhs.loan.entity.manager.RepaymentPlan;
import com.jhh.jhs.loan.entity.manager_vo.RepaymentPlanVo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface RepaymentPlanMapper extends Mapper<RepaymentPlan> {

    RepaymentPlan selectOnePlanByContractId(Integer contractId);

    int updateByBrroNum(RepaymentPlanVo record);

    Double selectAlsoRepay(String borrId);

    double selectCanPay(String borrId);

    List getRepaymentTermPlan(String borrowId);

    List selectByBorrId(String borrId);

    /**
     * 获取未还款的期数并且未逾期的期数
     *
     * @param borrId
     * @return
     */
    Double selectNotPayPlanByBorrId(Integer borrId);

    //更新repayment_plan表的collectionUser
    void updateCollectionUser(Map map);

    List<RepaymentPlan> getOverdueRepaymentPlan(String borrId);
}