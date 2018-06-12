package com.jhh.jhs.loan.app.service.capital;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jhh.jhs.loan.api.channel.AgentChannelService;
import com.jhh.jhs.loan.api.entity.ResponseDo;

/**
 * Copyright © 2018 上海金互行金融服务有限公司. All rights reserved. *
 *
 * @Title:
 * @Prject: jhs-loan
 * @Package: com.jhh.jhs.loan.app.app.capital.test
 * @Description: ${todo}
 * @author: jack liujialin@jinhuhang.com.cn
 * @date: 2018/1/23 15:08
 * @version: V1.0
 */

@org.springframework.stereotype.Service
public class AgentpayTestService {
//    @Reference(version = "1.0.1",group = "local")
    @Reference
    private AgentChannelService agentChannelService;

//    @Reference(version = "1.0.1",group = "local")

    //代付测试
   /* public String a(Integer userId, String borrId, Integer triggerStyle){
        String json = agentpayRealService.doAgentPay(userId,borrId,triggerStyle,null);
        return json;
    }*/

    //代付查询测试
    public String b(String serId) throws Exception {
        ResponseDo result = agentChannelService.state(serId);
        return result.getInfo();
    }
}
