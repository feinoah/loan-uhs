package com.jhh.jhs.loan.service.commission;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.jhh.jhs.loan.api.commission.CommissionOrderService;
import com.jhh.jhs.loan.api.commission.CommissionRuleService;
import com.jhh.jhs.loan.api.constant.Constants;
import com.jhh.jhs.loan.common.util.Detect;
import com.jhh.jhs.loan.entity.app.BorrowList;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.entity.share.CommissionRule;
import com.jhh.jhs.loan.mapper.app.BorrowListMapper;
import com.jhh.jhs.loan.mapper.app.PersonMapper;
import com.jhh.jhs.loan.mapper.share.CommissionRuleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * 佣金规则触发业务
 */
@Service
public class CommissionRuleServiceImpl implements CommissionRuleService {

    private static final Logger logger = LoggerFactory.getLogger(CommissionRuleServiceImpl.class);

    @Autowired
    PersonMapper personMapper;

    @Autowired
    CommissionRuleMapper commissionRuleMapper;


    @Autowired
    CommissionOrderService commissionOrderService;
    @Autowired
    BorrowListMapper borrowListMapper;

    @Override
    public NoteResult commissionCalculation(Integer perId, Integer trackingStatus) {
        return commissionCalculation(perId,trackingStatus, Constants.InviterLevel.LEVEL1, "", 0);
    }

    /**
     * 计算生成佣金
     * @param perId 用户id
     * @param trackingStatus 被邀请人状态
     * @param level 级别
     * @param device 设备
     * @param inviteePerId 被邀请人ID
     * @return
     */
    private NoteResult commissionCalculation(Integer perId, Integer trackingStatus, Integer level, String device, Integer inviteePerId) {
        logger.info("commissionCalculation:perID:" + perId + "---trackingStatus:" + trackingStatus + "---level:" +level );
        if(!Detect.isPositive(level)){
            return NoteResult.FAIL_RESPONSE("邀请级别不合法");
        }
        //递归出口,最大两级
        if(level > Constants.InviterLevel.LEVEL2){
            return NoteResult.SUCCESS_RESPONSE();
        }
        if(!Detect.isPositive(trackingStatus)){
            return NoteResult.FAIL_RESPONSE("佣金规则不存在");
        }
        //被邀请人
        inviteePerId = inviteePerId == 0 ?  perId : inviteePerId;

        Person person = personMapper.getPersonAndDevice(perId);
        if(person == null){
            return NoteResult.FAIL_RESPONSE("用户不存在");
        }
        //是否已经发放佣金
        if(commissionOrderService.checkCommissionRepeat(inviteePerId, person.getInviter(), trackingStatus, level)){
            logger.info("只有第一笔发放佣金perId:" + inviteePerId + "---trackingStatus:" + trackingStatus);
            return NoteResult.FAIL_RESPONSE("只有第一笔发放佣金");
        }

        if(Detect.notEmpty(device) || level == Constants.InviterLevel.LEVEL2){
            //如果有设备类型直接保存设备类型
            person.setDevice(device);
        }


        if(Detect.isPositive(person.getInviter())){
            //存在邀请人走佣金规则
            CommissionRule rule = rulesTouch(trackingStatus,person,level);
            if(rule != null){
                //规则匹配成功，保存佣金订单
                commissionOrderService.saveCommissionOrderByRule(rule, person.getInviter(), person.getDevice(), inviteePerId);
            }
        }else{
            //没有邀请人直接返回
            return NoteResult.SUCCESS_RESPONSE();
        }
        return commissionCalculation(person.getInviter(),trackingStatus, level + 1, person.getDevice(), person.getId());
    }

    /**
     * 触碰规则
     * @param trackingStatus
     * @param person
     * @param level
     * @return
     */
    private CommissionRule rulesTouch(Integer trackingStatus, Person person, Integer level){
        if(Detect.isPositive(trackingStatus) && person != null){
            //查询上级邀请用户
            Person perentPerson = personMapper.selectByPrimaryKey(person.getInviter());
            if(perentPerson != null){
                //规则处理集合
                List<CommissionRule> rules = rulesDispose(trackingStatus, perentPerson.getPhone(), level);

                if(Detect.notEmpty(rules)){
                    //选择触碰规则
                    return ruleChoose(rules, person.getDevice());
                }
            }

        }
        return null;
    }

    /**
     * 规则处理集合
     * @param trackingStatus
     * @param phone
     * @param level
     * @return
     */
    private List<CommissionRule> rulesDispose(Integer trackingStatus, String phone, Integer level){
        if(Detect.isPositive(trackingStatus) && Detect.notEmpty(phone) && Detect.isPositive(level)){
            //查询特殊用户规则
            Example example = new Example(CommissionRule.class);
            example.createCriteria().andEqualTo("trackingStatus",trackingStatus)
                    .andEqualTo("inviterLevel", level)
                    .andEqualTo("channelPhone",phone);
            List<CommissionRule> rules = commissionRuleMapper.selectByExample(example);

            if(!Detect.notEmpty(rules)){
                //非特殊规则用户
                example = new Example(CommissionRule.class);
                example.createCriteria().andEqualTo("trackingStatus",trackingStatus)
                        .andEqualTo("inviterLevel", level)
                        .andEqualTo("channelPhone","");

                return commissionRuleMapper.selectByExample(example);
            }
            return rules;
        }
       return null;
    }

    /**
     * 规则选择
     * @param rules
     * @param device
     * @return
     */
    private CommissionRule ruleChoose(List<CommissionRule> rules, String device){
        if(Detect.notEmpty(rules)){
            CommissionRule commissionRule = new CommissionRule();
            for (CommissionRule rule : rules){
                //匹配设备信息规则,否则返回null
                if(rule.getApplyPeople() == Constants.ApplyPeople.ANDROID && "android".equals(device)){
                    return  rule;
                }else if (rule.getApplyPeople() == Constants.ApplyPeople.IOS && "ios".equals(device)){
                    return  rule;
                }else if(rule.getApplyPeople() == Constants.ApplyPeople.ALL){
                    commissionRule = rule;
                }
            }
            return commissionRule;
        }
        return null;
    }
}
