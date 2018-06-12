package com.jhh.jhs.loan.manage.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jhh.jhs.loan.common.util.DateUtil;
import com.jhh.jhs.loan.entity.loan.Collectors;
import com.jhh.jhs.loan.entity.share.CommissionRule;
import com.jhh.jhs.loan.entity.utils.ManagerResult;

import com.jhh.jhs.loan.manage.mapper.CollectorsMapper;
import com.jhh.jhs.loan.manage.mapper.CommissionRuleMapper;
import com.jhh.jhs.loan.manage.service.Commission.CommissionRuleService;
import com.jhh.jhs.loan.manage.utils.Detect;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.MapperException;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CommissionRuleServiceImpl  implements CommissionRuleService {

    private static Logger log = Logger.getLogger(CommissionRuleService.class);
    @Autowired
    private CommissionRuleMapper commissionRuleMapper;
    @Autowired
    private CollectorsMapper collectorsMapper;
    /**
     * 获取规则列表
     * @return
     */
    @Override
    public PageInfo<CommissionRule> queryCommissionRuleList(Map<String,Object> queryMap,int offset, int size) {
         if(Detect.notEmpty(queryMap)){
            if(Detect.notEmpty(queryMap.get("operationUser") + "")){
                String userNo = collectorsMapper.getSysNoByName(queryMap.get("operationUser").toString());
                if(Detect.notEmpty(userNo)){
                    queryMap.put("operationUser",userNo);
                }
            }
        }
        Example queryExample = new Example(CommissionRule.class);
        Example.Criteria criteria = queryExample.createCriteria();
        handleCondition(queryMap, criteria);
        if (queryMap.containsKey("selector") && queryMap.containsKey("desc")) {
            Example.OrderBy orderBy = queryExample.orderBy(queryMap.get("selector").toString());
            if ("desc".equals(queryMap.get("desc"))) {
                orderBy.desc();
            }
        }
        PageHelper.offsetPage(offset, size);
        List<CommissionRule> commissionRules = commissionRuleMapper.selectByExample(queryExample);
        if(commissionRules!=null&&commissionRules.size()>0)
        for(int i=0;i<commissionRules.size();i++){
            CommissionRule commissionRule=commissionRules.get(i);
            if(null!=commissionRule.getOperationUser()&&!"".equals(commissionRule.getOperationUser())) {
                List<Collectors> list = collectorsMapper.selectUserBySysNo(commissionRule.getOperationUser());
                if(null!=list&&list.size()>0)
                    commissionRules.get(i).setOperationUser(list.get(0).getUserName());
                else
                    commissionRules.get(i).setOperationUser("");
            }
        }
        PageInfo<CommissionRule> pageInfo = new PageInfo<>(commissionRules);
        return pageInfo;
    }


    /**
     * 获取单个规则
     * @param id
     * @return
     */
    @Override
    public CommissionRule selectByPrimaryKey(Integer id){
        return commissionRuleMapper.selectByPrimaryKey(id);
    }

    /**
     * 删除规则列表
     * @param idfordel
     * @return
     */
    @Override
    public ManagerResult deleteCommissionRule(String idfordel) {
        String[] ruleId= idfordel.split(",");

        ManagerResult managerResult  = new ManagerResult();
        for (int i = 0; i < ruleId.length; i++) {
            try {
                int result =commissionRuleMapper.deleteByPrimaryKey(Integer.parseInt(ruleId[i]));
                managerResult.setCode(result);
                if(result>0){
                    managerResult.setMessage("处理成功！");
                }else{
                    managerResult.setMessage("处理失败！");
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return managerResult;
    }

    /**
     * 新增规则列表
     * @param commissionRule
     * @return
     */
    @Override
    public ManagerResult insertCommissionRule(CommissionRule commissionRule) {
        ManagerResult managerResult  = new ManagerResult();
        try {
            int result =commissionRuleMapper.insert(commissionRule);
            managerResult.setCode(result);
            if(result>0){
                managerResult.setMessage("处理成功！");
            }else{
                managerResult.setMessage("处理失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return managerResult;
    }

    /**
     * 修改规则列表
     * @param commissionRule
     * @return
     */
    @Override
    public ManagerResult updateCommissionRule(CommissionRule commissionRule) {
        ManagerResult managerResult  = new ManagerResult();
        try {
            int result=0;
            commissionRule.setUpdateDate(new Date());
            if(null==commissionRule.getId()){
                result=commissionRuleMapper.insert(commissionRule);
            }else{
                result =commissionRuleMapper.updateByPrimaryKeySelective(commissionRule);
            }

            managerResult.setCode(result);
            if(result>0){
                managerResult.setMessage("处理成功！");
            }else{
                managerResult.setMessage("处理失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return managerResult;
    }

    private static void handleCondition(Map<String, Object> conditionMap, Example.Criteria criteria) {
        for (String entry : conditionMap.keySet()) {
            try {
                if(entry.indexOf("updateDate") < 0 ){
                    criteria.andEqualTo(entry, conditionMap.get(entry));
                }else {
                    criteria.andBetween("updateDate",
                            DateUtil.stampToDate(conditionMap.get("updateDate_start") + ""),
                            DateUtil.stampToDate(conditionMap.get("updateDate_end") + ""));
                }
            } catch (MapperException e) {
                log.error(e);
            }
        }
    }
}
