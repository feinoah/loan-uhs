package com.jhh.jhs.loan.api.commission;

import com.jhh.jhs.loan.entity.share.CommissionRule;

/**
 * 邀请好友佣金流水
 * @author carl.wan
 * 2018年4月17日 15:09:12
 */
public interface CommissionOrderService {

    /**
     * 保存佣金流水
     * @param rule 触碰规则
     * @param inviterId 邀请人Id
     * @param device 设备类型
     * @param inviteePerId 被邀请人Id
     * @return
     */
    int saveCommissionOrderByRule(CommissionRule rule, Integer inviterId,String device, Integer inviteePerId);
    /**
     * 校验是否重复发放佣金
     * @param perId 被邀请人
     * @param inviter 邀请人
     * @param trackingStatus 被邀请人状态 1.已注册、2.已放款、3.已还第一期、4.已还第二期、5.已还第三期、6.已还清
     * @param level 佣金规则触发邀请人级别：1:1级邀请人、2:2级邀请人
     * @return
     */
    boolean checkCommissionRepeat(Integer perId,Integer inviter,Integer trackingStatus, Integer level);
}
