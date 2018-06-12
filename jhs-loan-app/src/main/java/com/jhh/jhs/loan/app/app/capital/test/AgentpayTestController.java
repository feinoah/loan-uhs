package com.jhh.jhs.loan.app.app.capital.test;

import com.jhh.jhs.loan.app.service.capital.AgentpayTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

@RestController
public class AgentpayTestController {
    @Autowired
    private AgentpayTestService agentpayTestService;
    //代付测试
    @RequestMapping("/agentpay")
    public String a(Integer userId, String borrId, Integer triggerStyle){
      //  String json = agentpayTestService.(userId,borrId,triggerStyle);
        return null;
    }

    //代付查询测试
    @RequestMapping("/agentpay/query")
    public String b(String serId) throws Exception {
        String result = agentpayTestService.b(serId);
        return result;
    }
}
