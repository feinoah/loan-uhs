package com.jhh.jhs.loan.task;

import com.jhh.jhs.loan.service.CommissionSummaryService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 佣金汇总定时任务
 * @author xingmin
 */
@Component
@Log4j
public class CommissionSummaryTask {

    @Autowired
    private CommissionSummaryService commissionSummaryService;

    /**
     * 每天凌晨1点汇总用户佣金信息
     */
    @Scheduled(cron = "0 0 01 * * ?")
    public void doBussiness() {
        log.info("do commission summary begin....");
        commissionSummaryService.doBussiness();
        log.info("do commission summary end....");
    }

}
