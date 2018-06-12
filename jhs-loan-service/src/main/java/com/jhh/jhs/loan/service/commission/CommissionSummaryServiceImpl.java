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
import com.jhh.jhs.loan.entity.share.CommissionSummary;
import com.jhh.jhs.loan.mapper.app.PersonMapper;
import com.jhh.jhs.loan.mapper.share.CommissionOrderMapper;
import com.jhh.jhs.loan.mapper.share.CommissionSummaryMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * 佣金汇总业务
 */
@Service
public class CommissionSummaryServiceImpl implements CommissionSummaryService {

    private static final Logger logger = LoggerFactory.getLogger(CommissionSummaryServiceImpl.class);

    @Autowired
    private CommissionSummaryMapper commissionSummaryMapper;
    @Autowired
    private PersonMapper personMapper;

    @Override
    public int saveCommissionSummary(CommissionOrder commissionOrder) {

        if(commissionOrder != null ){
            logger.info("saveCommissionSummary id = " + commissionOrder.getId());
            //邀请级别
            if(Constants.InviterLevel.LEVEL1.equals(commissionOrder.getInviterLevel())){
                CommissionSummary  commissionSummary = commissionSummaryMapper.queryCommissionByPersonId(commissionOrder.getInviterLevel1() + "");
                //一级有数据更新，无数据初始化
                if(commissionSummary != null){
                    commissionSummary.setCommissionTotal(commissionSummary.getCommissionTotal().add(commissionOrder.getCommissionAmount()));
                    if(commissionOrder.getTriggerGroup() == Constants.TrackingStatus.REGISTER){
                        //注册阶段才加人数
                        commissionSummary.setInviterLevel1Count(commissionSummary.getInviterLevel1Count() + 1);
                    }
                    commissionSummary.setUpdateDate(Calendar.getInstance().getTime());
                    return commissionSummaryMapper.updateByPrimaryKeySelective(commissionSummary);
                }else{
                    initCommissionSummary(commissionOrder);
                }
            }else if(Constants.InviterLevel.LEVEL2.equals(commissionOrder.getInviterLevel())){
                CommissionSummary  commissionSummary = commissionSummaryMapper.queryCommissionByPersonId(commissionOrder.getInviterLevel2() + "");
                //二级只存在更新
                if(commissionSummary != null){
                    commissionSummary.setCommissionTotal(commissionSummary.getCommissionTotal().add(commissionOrder.getCommissionAmount()));
                    if(commissionOrder.getTriggerGroup() == Constants.TrackingStatus.REGISTER){
                        //注册阶段才加人数
                        commissionSummary.setInviterLevel2Count(commissionSummary.getInviterLevel2Count() + 1);
                    }
                    commissionSummary.setUpdateDate(Calendar.getInstance().getTime());
                    return commissionSummaryMapper.updateByPrimaryKeySelective(commissionSummary);
                }
            }
        }
        return 0;
    }

    private void initCommissionSummary(CommissionOrder commissionOrder){
        if(commissionOrder != null ){
            CommissionSummary commissionSummary = new CommissionSummary();
            //确定邀请人级别
            if(Constants.InviterLevel.LEVEL1.equals(commissionOrder.getInviterLevel())){
                commissionSummary.setPerId(commissionOrder.getInviterLevel1());
                commissionSummary.setPhone(commissionOrder.getInviterPhoneLevel1());
            }else if(Constants.InviterLevel.LEVEL2.equals(commissionOrder.getInviterLevel())){
                commissionSummary.setPerId(commissionOrder.getInviterLevel2());
                commissionSummary.setPhone(commissionOrder.getInviterPhoneLevel2());
            }
            Person person = personMapper.selectByPrimaryKey(commissionSummary.getPerId());

            if(person != null){
                if(Detect.isPositive(person.getInviter())){
                    Person inviterPerson = personMapper.selectByPrimaryKey(person.getInviter());
                    //邀请人不为空
                    commissionSummary.setInviterId(inviterPerson.getId());
                    commissionSummary.setInviterPhone(inviterPerson.getPhone());
                }
            }
            commissionSummary.setCommissionTotal(commissionOrder.getCommissionAmount());
            commissionSummary.setCommissionBalance(new BigDecimal(0));
            commissionSummary.setInviterLevel1Count(1);
            commissionSummary.setInviterLevel2Count(0);
            commissionSummary.setUpdateDate(Calendar.getInstance().getTime());
            commissionSummaryMapper.insertSelective(commissionSummary);
        }
    }
}
