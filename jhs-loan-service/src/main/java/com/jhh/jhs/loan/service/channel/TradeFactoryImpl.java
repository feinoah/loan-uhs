package com.jhh.jhs.loan.service.channel;

import com.jhh.jhs.loan.api.channel.TradeFactory;
import com.jhh.jhs.loan.api.channel.TradePayService;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 *  只调用代付代扣接口 不连接诶业务
 */
@Component
public class TradeFactoryImpl implements TradeFactory, ApplicationContextAware, DisposableBean {

    private  ApplicationContext applicationContext;

    @Override
    public <T extends TradePayService> T createBean(String payChannel) {
        return  getBean(payChannel);
    }

    @Override
    public void destroy() throws Exception {
        clearHolder();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    private void clearHolder() {
        applicationContext = null;
    }

    //获得applicationContext
    private ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @SuppressWarnings("unchecked")
    private <T> T getBean(String name) {
        assertContextInjected();
        return (T) getApplicationContext().getBean(name);
    }

    //判断application是否为空
    private void assertContextInjected() {
        Validate.isTrue(applicationContext != null, "application未注入 ，请在springContext.xml中注入SpringHolder!");
    }
}
