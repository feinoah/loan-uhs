package com.jhh.jhs.loan.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jhh.jhs.loan.api.constant.Constants;
import com.jhh.jhs.loan.api.refund.RefundService;
import com.jhh.jhs.loan.dao.LoanOrderDOMapper;
import com.jhh.jhs.loan.model.LoanOrderDO;
import com.jhh.jhs.loan.model.LoanOrderDOExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 定时任务<br />
 * 扫描退款的订单
 */
@Component
@Slf4j
public class AgentRefundQueryTask {

    @Autowired
    private LoanOrderDOMapper loanOrderDOMapper;
    @Reference(interfaceClass = RefundService.class)
    private RefundService refundService;

    /**
     * 每隔1分钟查询一次
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void doRefund() {

        log.info("开始调用定时任务退款=========");
        LoanOrderDOExample loanOrderDOExample = new LoanOrderDOExample();
        LoanOrderDOExample.Criteria cia = loanOrderDOExample.createCriteria();
        List<String> list = new ArrayList<>();
        list.add(Constants.payOrderType.HAIER_REFUND_PAY_TYPE);
        list.add(Constants.payOrderType.YSB_REFUND_PAY_TYPE);
        list.add(Constants.payOrderType.PAYCENTER_REFUND_PAY_TYPE);
        cia.andTypeIn(list);
        cia.andRlStateEqualTo("p");
        Calendar beforeTime = Calendar.getInstance();
        beforeTime.add(Calendar.MINUTE, -3);
        cia.andCreationDateLessThan(beforeTime.getTime());
        List<LoanOrderDO> loanOrderDOList = loanOrderDOMapper.selectByExample(loanOrderDOExample);

        log.info("获取需要退款服务状态的单子开始:在处理的退款单子数量：count = {}", (loanOrderDOList == null ? 0 : loanOrderDOList.size()));

        if (!CollectionUtils.isEmpty(loanOrderDOList)) {
            loanOrderDOList.forEach(v -> refundService.refundState(v.getSerialNo()));
        }
        log.info("定时任务退款更新状态结束。。。。");
    }
}
