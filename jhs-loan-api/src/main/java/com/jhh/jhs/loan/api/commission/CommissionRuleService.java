package com.jhh.jhs.loan.api.commission;

import com.jhh.jhs.loan.entity.app.NoteResult;

/**
 * 邀请好友佣金规则
 * @author carl.wan
 * 2018年4月17日 15:09:12
 */
public interface CommissionRuleService {

    /**
     * 计算生成佣金
     * @param perId 用户ID
     * @param trackingStatus 被邀请人状态 1.已注册、2.已放款、3.已还第一期、4.已还第二期、5.已还第三期、6.已还清
     * @return
     */
    NoteResult commissionCalculation(Integer perId, Integer trackingStatus);

}
