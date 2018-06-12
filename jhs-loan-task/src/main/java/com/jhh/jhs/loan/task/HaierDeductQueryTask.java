package com.jhh.jhs.loan.task;

import com.jhh.jhs.loan.api.capital.HaierCallBackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 2018/3/15.
 */
@Component
@Slf4j
@SuppressWarnings("SpringJavaAutowiringInspection")
public class HaierDeductQueryTask {

    @Autowired
    private HaierCallBackService haierCallBackService;

  //  @Scheduled(fixedRate = 5*60*1000) // 间隔30分钟执行   测试间隔5分钟
    public void haierTaskCycle() {
        haierCallBackService.queryTrade();
    }

    /**
     * 半个小时查询一次主动还款的信息，对认证支付成功和失败的进行操作。
     */
  //  @Scheduled(fixedRate = 5*60*1000) // 间隔30分钟执行  测试间隔5分钟
    public void haierQueryPayment() {
        haierCallBackService.queryAppRpay();
    }
}
