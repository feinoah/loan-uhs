package com.jhh.jhs.loan.task;

import com.jhh.jhs.loan.service.UserNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by wanzezhong on 2018/5/30.
 */
@Component
public class UserNoticTask {

    @Autowired
    private UserNoticeService userNoticeService;

    /**
     *用户下载注册成功一分钟未下载app
     */
    @Scheduled(fixedRate = 60 * 1000) // 间隔1分钟执行
    public void registerNotice() {
        userNoticeService.registerNotice();
    }

    /**
     * 用户下载App30分钟未登录
     */
    // TODO 百可录时间生产更新为5分钟触发
    @Scheduled(fixedRate = 60 * 1000 * 1) // 间隔5分钟执行
    public void loginNotice() {
        userNoticeService.loginNotice();
    }
}
