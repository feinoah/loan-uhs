package com.jhh.jhs.loan.app.app.capital;

import com.jhh.jhs.loan.api.app.LoanService;
import com.jhh.jhs.loan.api.channel.AgentChannelService;
import com.jhh.jhs.loan.api.constant.Constants;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.AgentDeductRequest;
import com.jhh.jhs.loan.common.constant.PayCenterChannelConstant;
import com.jhh.jhs.loan.common.util.RedisConst;
import com.jhh.jhs.loan.entity.app.NoteResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisCluster;

/**
 * Copyright © 2018 上海金互行金融服务有限公司. All rights reserved. *
 *
 * @Title: 代扣控制器
 * @author: luolong
 * @date: 2018/1/24
 */

@RestController
@Slf4j
@SuppressWarnings("SpringJavaAutowiringInspection")
@Api(value = "代扣接口", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class AgentDeductController extends MyBaseController<ResponseDo> {

    @Autowired
    private AgentChannelService agentChannelService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private JedisCluster jedisCluster;

    @ApiOperation(value = "代扣接口", notes = "代扣接口", response = NoteResult.class)
    @RequestMapping(value = "/payCenter/agentDeduct", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ResponseDo> doAgentDeduct(@ApiParam(value = "body", name = "代扣请求体") @Validated AgentDeductRequest request, BindingResult result) {
        log.info("\n=========== 代扣接口 come in ===========");
        log.info("\n前端请求数据:{}", request.toString());
        ResponseEntity<ResponseDo> responseEntity;
        try {
            if (result.hasErrors()) {
                throw new JhsNotifyException(ResultStatusEnum.PARAM_ERROR.getCode(), ResultStatusEnum.PARAM_ERROR.getMessage() + "|" + result.getAllErrors().get(0).getDefaultMessage());
            }
            if(!PayCenterChannelConstant.PAY_CHANNEL_ZFB.equals(request.getPayChannel())){
                // 判断验证码是否正确
                String valiCodeKey = new StringBuilder(RedisConst.VALIDATE_CODE).append(RedisConst.SEPARATOR).append(request.getPhone()).toString();
                String valiCode = jedisCluster.get(valiCodeKey);

                if(StringUtils.isEmpty(valiCode)){
                    return buildResp(Constants.CommonPayResponseConstants.VALIDATE_ERROR_CODE, "请先获取验证码!");
                }
                if(!PayCenterChannelConstant.PAY_CHANNEL_HLB_QUICK.equals(valiCode) && !request.getValidateCode().equals(valiCode)){
                    return buildResp(Constants.CommonPayResponseConstants.VALIDATE_ERROR_CODE, "验证码不正确!");
                }
            }
            responseEntity = doBusiness(request);

        } catch (Exception e) {
            responseEntity = buildResp(201, ResultStatusEnum.PARAM_ERROR.getMessage() + "|" + result.getAllErrors().get(0).getDefaultMessage());
            log.error("\n...代扣接口异常 ...", e);
        }

        return responseEntity;
    }

    @ApiOperation(value = "代扣查询接口", notes = "代扣查询接口", response = NoteResult.class)
    @RequestMapping(value = "/payCenter/agentState", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ResponseDo> doAgentState(@ApiParam(value = "body", name = "查询请求体") @Validated String serNo) {
        log.info("\n=========== 代扣查询接口 come in ===========");
        log.info("\n前端查询流水号：", serNo);
        ResponseEntity<ResponseDo> responseEntity;
        try {

            ResponseDo responseDo = agentChannelService.state(serNo);
            responseEntity = buildResp(responseDo.getCode(), responseDo.getInfo());
        } catch (Exception e) {
            responseEntity = buildResp(Constants.CommonPayResponseConstants.BUSINESS_ERROR_CODE, "失败");
            log.error("查询发生错误：", e);
        }
        return responseEntity;
    }

    @Override
    public ResponseEntity<ResponseDo> doBusiness(Object object) {
        log.info("\n=========== 代扣接口 doBusiness ===========");
        AgentDeductRequest request = (AgentDeductRequest) object;
        ResponseDo result = new ResponseDo();
        try {
            // 根据borrId查询对应的催收人信息
            String userSysno = loanService.getCollectionUser(Integer.parseInt(request.getBorrNum()));
            if (StringUtils.isNotEmpty(userSysno)) {
                request.setCollectionUser(userSysno);
            }

            result = agentChannelService.deduct(request);

        } catch (Exception e) {
            result.setCode(Constants.CommonPayResponseConstants.SYSTEM_ERROR_CODE);
            result.setInfo("系统繁忙,请稍后再试");
            log.info("系统异常", e);
        }
        return buildResp(result.getCode(), result.getInfo(),result.getData());


    }
}
