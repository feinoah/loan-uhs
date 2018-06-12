package com.jhh.jhs.loan.api.share;

import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.share.InviteInfo;

/**
 * Created by chenchao on 2018/3/14.
 */
public interface CommissionShareService {

    /**
     * 根据用户ID 查询用户佣金信息
     * @param personId 用户ID
     * @return InviteInfo 佣金信息
     */
    InviteInfo queryCommissionByPersonId(String personId);

    /**
     * 佣金提现
     * @param personId 用户ID
     * @return NoteResult 提现结果
     * @throws Exception 异常信息
     */
    NoteResult commissionWithDraw(String personId) throws Exception;

}
