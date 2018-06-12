package com.jhh.jhs.loan.manage.service.Commission;

import com.github.pagehelper.PageInfo;
import com.jhh.jhs.loan.entity.share.CommissionRule;
import com.jhh.jhs.loan.entity.utils.ManagerResult;

import java.util.Map;

public interface CommissionRuleService {

    /**
     * 获取规则列表
     * @return
     */
    public PageInfo<CommissionRule> queryCommissionRuleList(Map<String,Object> queryMap,int offset, int size);

    /**
     * 获取单个规则
     * @param id
     * @return
     */
    public CommissionRule   selectByPrimaryKey(Integer id);

    /**
     * 删除规则列表
     * @param idfordel
     * @return
     */
    public ManagerResult deleteCommissionRule(String idfordel);

    /**
     * 新增规则列表
     * @param commissionRule
     * @return
     */
    public ManagerResult insertCommissionRule(CommissionRule commissionRule);

    /**
     * 修改规则列表
     * @param commissionRule
     * @return
     */
    public ManagerResult updateCommissionRule(CommissionRule commissionRule);
}
