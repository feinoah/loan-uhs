package com.jhh.jhs.loan.service.haier;


import com.jhh.jhs.loan.entity.HaierDeductVo;
import com.jhh.jhs.loan.entity.HaierPayVo;
import com.jhh.jhs.loan.entity.app.NoteResult;

/**
 * Created by chenchao on 2017/9/13.
 * 代付相关操作
 */
public interface HaierPayService {

    /***
     *    银行卡代付 网关接口
     * @param pay
     * @return
     * @throws Exception
     */
     NoteResult partnerBankPaying(HaierPayVo pay) throws Exception;



}
