package com.jhh.jhs.loan.app.app.capital;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jhh.jhs.loan.api.channel.AgentChannelService;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.AgentPayRequest;
import com.jhh.jhs.loan.api.entity.capital.AgentpayRO;
import com.jhh.jhs.loan.entity.app.NoteResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Copyright © 2018 上海金互行金融服务有限公司. All rights reserved. *
 *
 * @Title:
 * @Prject: jhs-loan
 * @Package: com.jhh.jhs.loan.app.app.capital
 * @Description: 代付api
 * @author: jack liujialin@jinhuhang.com.cn
 * @date: 2018/1/22 10:29
 * @version: V1.0
 */

@RestController
@Slf4j
@SuppressWarnings("SpringJavaAutowiringInspection")
@Api(value = "代付接口", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class AgentpayController extends MyBaseController {

    @Reference
    private AgentChannelService agentChannelService;


    @ApiOperation(value = "代付接口", notes = "代付接口", response = NoteResult.class)
    @RequestMapping(value = "/payCenter/agentPay", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<NoteResult> doAgentPay(@ApiParam(value = "body", name = "代付请求体") @Validated AgentpayRO agentpayRO, BindingResult result) {
        log.info("\n=========== 代付接口 come in ===========");
        log.info("\n前端请求数据:{}", agentpayRO.toString());
        ResponseEntity<NoteResult> responseEntity = null;
        try {
            if (result.hasErrors()) {
                throw new JhsNotifyException(ResultStatusEnum.PARAM_ERROR.getCode(), ResultStatusEnum.PARAM_ERROR.getMessage() + "|" + result.getAllErrors().get(0).getDefaultMessage());
            }
            responseEntity = doBusiness(agentpayRO);

        } catch (Exception e) {
            if (e instanceof JhsNotifyException) {
                responseEntity = buildErrorResp((JhsNotifyException) e);
                log.error("\n...代付接口 ...{} ", ((JhsNotifyException) e).getDesc());

            } else {
                responseEntity = buildErrorResp(new JhsNotifyException(ResultStatusEnum.SYSTEM_ERROR.getCode(), ResultStatusEnum.SYSTEM_ERROR.getMessage()));
                log.error("\n...代付接口异常 ...", e);
            }
        }
        log.info("\n=========== rechage->doRechage come out ===========");
        return responseEntity;
    }

    @Override
    public ResponseEntity<NoteResult> doBusiness(Object object) throws Exception {

        log.info("\n=========== 代付接口 doBusiness ===========");
        AgentpayRO agentpayRO = (AgentpayRO) object;
        ResponseDo result = agentChannelService.pay(new AgentPayRequest(Integer.valueOf(agentpayRO.getUserId()), agentpayRO.getBorrowId(), agentpayRO.getTriggerStyle(), null));
        if (200 == result.getCode()) {
            log.info("\n---代付 第三方处理中");
            return buildSuccessResp(null, null);
        } else {
            log.info("\n---代付 内部错误");
            throw new JhsNotifyException(ResultStatusEnum.SYSTEM_ERROR.getCode(), ResultStatusEnum.SYSTEM_ERROR.getMessage());
        }
    }

}
