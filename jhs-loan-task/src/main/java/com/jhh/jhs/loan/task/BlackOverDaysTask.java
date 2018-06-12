package com.jhh.jhs.loan.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jhh.jhs.loan.api.black.RiskBlackService;
import com.jhh.jhs.loan.common.util.DateUtil;
import com.jhh.jhs.loan.dao.BorrowListMapper;
import com.jhh.jhs.loan.entity.app_vo.BorrowVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 逾期11天拉黑
 */
@Component
@Slf4j
public class BlackOverDaysTask {

    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private RiskBlackService riskBlackService;

    /**
     * 每天下午5点开始执行此定时
     */
    @Scheduled(cron = "0 0 17 * * ?")
    public void black() {
        System.out.println("执行时间========================="+ DateUtil.getDateString(new Date(),"yyyy-MM-dd hh:mm:ss"));
        log.info("【逾期11天用户拉黑】定时任务开始。。。。");
        List<BorrowVO> voList = borrowListMapper.selectOverDays();

        if (!CollectionUtils.isEmpty(voList)) {
            voList.forEach(vo -> {
                riskBlackService.black(vo.getPhone(), vo.getCardNum(), vo.getPersonName(), vo.getPerId());
            });
        }

        log.info("【逾期11天用户拉黑】定时任务结束。。。。");
    }
}
