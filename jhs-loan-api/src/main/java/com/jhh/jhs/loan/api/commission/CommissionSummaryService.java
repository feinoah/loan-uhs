package com.jhh.jhs.loan.api.commission;

import com.jhh.jhs.loan.entity.share.CommissionOrder;

/**
 * 邀请好友佣金流水汇总
 * @author carl.wan
 * 2018年4月17日 15:09:12
 */
public interface CommissionSummaryService {

    /**
     * 保存佣金汇总
     * @param commissionOrder
     * @return
     */
    int saveCommissionSummary(CommissionOrder commissionOrder);

}
