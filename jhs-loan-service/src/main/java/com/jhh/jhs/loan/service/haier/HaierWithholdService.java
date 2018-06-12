package com.jhh.jhs.loan.service.haier;


import com.jhh.jhs.loan.entity.HaierDeductVo;
import com.jhh.jhs.loan.entity.app.NoteResult;

/**
 * Created by jbq on 2017/9/13.
 * 代扣相关操作
 */
public interface HaierWithholdService {

    /***
     *    银行卡代扣 网关接口
     * @param withhold
     * @return
     * @throws Exception
     */
     NoteResult partnerBankWithholding(HaierDeductVo withhold) throws Exception;



}
