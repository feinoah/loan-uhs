package com.jhh.jhs.loan.manage.controller.finance;


import com.jhh.jhs.loan.manage.entity.RefundRecordVo;
import com.jhh.jhs.loan.manage.service.refund.RefundRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("refundRecord")
@Slf4j
public class RefundRecordController {
    @Autowired
    RefundRecordService refundRecordService;
    /**
     * 退款流水
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/refund", method = RequestMethod.POST)
    public List<RefundRecordVo> getRefundRecord() {
        return refundRecordService.getRefundRecord();
    }
}
