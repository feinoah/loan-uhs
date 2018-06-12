package com.jhh.jhs.loan.api.app;

import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.loan_vo.BatchCollectEntity;
import com.jhh.jhs.loan.entity.payment.Gather;

import java.util.List;

/**
 * 支付接口
 */
public interface PaymentService {

    /**
     * 单笔代收
     * @param gather
     * @return
     */
    NoteResult sigleGatherByLakala(Gather gather);

    /**
     * 批量代收
     * @return
     */
    NoteResult batchGatherByLakala(List<BatchCollectEntity> gathers);
}
