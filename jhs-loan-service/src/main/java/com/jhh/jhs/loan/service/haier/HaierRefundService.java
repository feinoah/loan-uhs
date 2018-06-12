package com.jhh.jhs.loan.service.haier;

import com.jhh.jhs.loan.entity.HaierRefundVo;
import com.jhh.jhs.loan.entity.app.NoteResult;

/**
 * 退款相关接口
 */
public interface HaierRefundService {

    /***
     *    银行卡退款 网关接口
     * @param refund
     * @return
     * @throws Exception
     */
    NoteResult partnerBankRefund(HaierRefundVo refund) throws Exception;
}
