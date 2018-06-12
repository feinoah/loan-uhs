package com.jhh.jhs.loan.service.impl;

import com.jhh.jhs.loan.api.notice.UserBehaviorNoticeService;
import com.jhh.jhs.loan.service.UserNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wanzezhong on 2018/5/30.
 */
@Service
public class UserNoticeServiceImpl implements UserNoticeService{

    @Autowired
    private UserBehaviorNoticeService userBehaviorNoticeService;

    @Override
    public void registerNotice() {
        userBehaviorNoticeService.registerNotice();
    }

    @Override
    public void loginNotice() {
        userBehaviorNoticeService.loginNotice();
    }
}
