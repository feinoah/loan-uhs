package com.jhh.jhs.loan.service.commission;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.jhh.jhs.loan.api.commission.CommissionOrderService;
import com.jhh.jhs.loan.api.commission.CommissionSummaryService;
import com.jhh.jhs.loan.api.constant.Constants;
import com.jhh.jhs.loan.common.util.Detect;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.entity.share.CommissionOrder;
import com.jhh.jhs.loan.entity.share.CommissionRule;
import com.jhh.jhs.loan.mapper.app.PersonMapper;
import com.jhh.jhs.loan.mapper.share.CommissionOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * 佣金订单业务
 */
@Service
public class CommissionOrderServiceImpl implements CommissionOrderService {

    private static final Logger logger = LoggerFactory.getLogger(CommissionOrderServiceImpl.class);

    @Autowired
    CommissionOrderMapper commissionOrderMapper;
    @Autowired
    PersonMapper personMapper;
    @Autowired
    CommissionSummaryService commissionSummaryService;

    @Override @Transactional
    public int saveCommissionOrderByRule(CommissionRule rule, Integer inviterId,String device, Integer inviteePerId) {
        if(rule != null && Detect.isPositive(inviterId) ){
            logger.info("saveCommissionOrderByRule:ruleID" + rule.getId() + "inviterId" + inviterId);
            Person person = personMapper.selectByPrimaryKey(inviteePerId);
            Person faterPerson = personMapper.selectByPrimaryKey(inviterId);
            if(person != null && faterPerson != null){
                CommissionOrder commissionOrder = new CommissionOrder();
                commissionOrder.setPerId(person.getId());
                commissionOrder.setDevice(device);
                commissionOrder.setPhone(person.getPhone());
                //触碰级别
                if(rule.getInviterLevel() == Constants.InviterLevel.LEVEL1){
                    commissionOrder.setInviterLevel1(faterPerson.getId());
                    if(faterPerson != null){
                        commissionOrder.setInviterPhoneLevel1(faterPerson.getPhone());
                    }
                }else if(rule.getInviterLevel() == Constants.InviterLevel.LEVEL2){
                    commissionOrder.setInviterLevel2(faterPerson.getId());
                    if(faterPerson != null){
                        commissionOrder.setInviterPhoneLevel2(faterPerson.getPhone());
                    }
                }
                //是否是特殊渠道
                if(Detect.notEmpty(rule.getChannelPhone())){
                    commissionOrder.setChannel(faterPerson.getId());
                    commissionOrder.setChannelPhone(rule.getChannelPhone());
                }
                commissionOrder.setRuleId(rule.getId());
                commissionOrder.setTriggerGroup(rule.getApplyPeople());
                commissionOrder.setTrackingStatus(rule.getTrackingStatus());
                commissionOrder.setInviterLevel(rule.getInviterLevel());
                commissionOrder.setWithdrawStatus(0);
                commissionOrder.setCommissionAmount(BigDecimal.valueOf(Float.valueOf(rule.getCommission())));
                commissionOrder.setCreationDate(Calendar.getInstance().getTime());

                //保存佣金订单总额
                commissionSummaryService.saveCommissionSummary(commissionOrder);
                //插入佣金订单
                return commissionOrderMapper.insertSelective(commissionOrder);
            }


        }

        return 0;
    }

    @Override
    public boolean checkCommissionRepeat(Integer perId, Integer inviter, Integer trackingStatus, Integer level) {
        if(Detect.isPositive(perId) && Detect.isPositive(inviter) && Detect.isPositive(trackingStatus) && Detect.isPositive(level)){

            CommissionOrder commissionOrder = new CommissionOrder();
            commissionOrder.setPerId(perId);
            commissionOrder.setTrackingStatus(trackingStatus);
            commissionOrder.setInviterLevel(level);

            if(level == Constants.InviterLevel.LEVEL1){
                commissionOrder.setInviterLevel1(inviter);
            }else if(level == Constants.InviterLevel.LEVEL2){
                commissionOrder.setInviterLevel2(inviter);
            }
            //如果存在则佣金订单
            if(Detect.notEmpty(commissionOrderMapper.selectByConditions(commissionOrder))){
                return true;
            }
        }

        return false;
    }
}
