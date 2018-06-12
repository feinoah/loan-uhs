package com.jhh.jhs.loan.service.sms;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.remoting.p2p.exchange.ExchangePeer;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.constant.StateCode;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.sms.SmsService;
import com.jhh.jhs.loan.common.util.EmaySmsUtil;
import com.jhh.jhs.loan.common.util.WuXunSmsUtil;
import com.jhh.jhs.loan.constant.Constant;
import com.jhh.jhs.loan.entity.manager.SmsTemplate;
import com.jhh.jhs.loan.mapper.sms.SmsTemplateMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tk.mybatis.mapper.util.StringUtil;

import java.util.Arrays;

/**
 * 2017/12/27.
 */
@Service
public class SmsServiceImpl implements SmsService{


    private static final Logger logger = LoggerFactory
            .getLogger(SmsServiceImpl.class);

    @Value("${isSendSms}")
    private String isSendSms;

    @Value("${agreeSendSmsIds}")
    private String agreeSendSmsIds;

    @Autowired
    private SmsTemplateMapper smsTemplateMapper;

    @Override
    public ResponseDo sendSms(int templateSeq, String phone, String... smsTemplateArgs) {
        logger.info("发送短信 ----templateSeq =" + templateSeq + ", phone = " + phone + ", 参数 = " + Arrays.toString(smsTemplateArgs));
        ResponseDo responseDo = new ResponseDo();
        responseDo.setData("");
        try{
        //验证是否需要发送短信
        if (!agreeSendSmsIds.contains(String.valueOf(templateSeq))) {
            responseDo.setCode(StateCode.SYSTEM_CODE);
            responseDo.setInfo(StateCode.SYSTEM_MSG);
            return responseDo;
        }
        if (StringUtils.isAnyEmpty(smsTemplateArgs)) {
            responseDo.setCode(StateCode.SMS_EMPTY_CODE);
            responseDo.setInfo(StateCode.SMS_EMPTY_MSG);
            return responseDo;
        }
        SmsTemplate sms = smsTemplateMapper.getSmsTemplate(templateSeq);
        logger.info("短信模板信息 SmsTemplate = " + JSONObject.toJSONString(sms));
        if (sms == null) {
            responseDo.setCode(StateCode.MSG_EMPTY_CODE);
            responseDo.setInfo(StateCode.MSG_EMPTY_MSG);
            return responseDo;
        }
        String message = String.format(sms.getContent(), (String[]) smsTemplateArgs);
        boolean flag=EmaySmsUtil.send(message, phone, 4);
        if(flag) {
            logger.info("短信发送成功phone="+phone+",message="+message+"");
            responseDo.setCode(StateCode.SUCCESS_CODE);
            responseDo.setInfo(StateCode.SUCCESS_MSG);
            responseDo.setData(message);
        }else{
            logger.info("短信发送失败phone="+phone+",message="+message+"");
            responseDo.setCode(StateCode.SYSTEM_CODE);
            responseDo.setInfo(StateCode.SYSTEM_MSG);
        }
    }catch(Exception e){
            responseDo.setCode(StateCode.SYSTEM_CODE);
            responseDo.setInfo(StateCode.SYSTEM_MSG);
            return responseDo;
    }
        return responseDo;
    }

    @Override
    public ResponseDo sendSms(int templateSeq, String phone) {
        logger.info("发送短信 ----templateSeq ="+templateSeq+"\nphone = "+phone);
        ResponseDo responseDo = new ResponseDo();
        responseDo.setData("");
        //验证是否需要发送短信
        if (!agreeSendSmsIds.contains(String.valueOf(templateSeq))) {
            responseDo.setCode(StateCode.SYSTEM_CODE);
            responseDo.setInfo(StateCode.SYSTEM_MSG);
            return responseDo;
        }
        SmsTemplate sms = smsTemplateMapper.getSmsTemplate(templateSeq);
        logger.info("短信模板信息 SmsTemplate = " + JSONObject.toJSONString(sms));
        if (sms == null){
            responseDo.setCode(StateCode.MSG_EMPTY_CODE);
            responseDo.setInfo(StateCode.MSG_EMPTY_MSG);
            return responseDo;
        }
        boolean flag=EmaySmsUtil.send(sms.getContent(), phone, 4);
        if(flag) {
            logger.info("短信发送成功phone="+phone+",message="+sms.getContent()+"");
            responseDo.setCode(StateCode.SUCCESS_CODE);
            responseDo.setInfo(StateCode.SUCCESS_MSG);
            responseDo.setData(sms.getContent());
        }else{
            logger.info("短信发送失败phone="+phone+",message="+sms.getContent()+"");
            responseDo.setCode(StateCode.SYSTEM_CODE);
            responseDo.setInfo(StateCode.SYSTEM_MSG);
        }
        return responseDo;
    }
    /**
     *吴迅催收短信
     * @param templateSeq
     * @param phone
     * @param smsTemplateArgs
     * @return
     */
    @Override
    public ResponseDo overdueSms(int templateSeq, String phone, String... smsTemplateArgs) {
        logger.info("发送短信 ----templateSeq =" + templateSeq + ", phone = " + phone + ", 参数 = " + Arrays.toString(smsTemplateArgs));
        ResponseDo responseDo = new ResponseDo();
        responseDo.setData("");
        try{
            //验证是否需要发送短信
            if (!agreeSendSmsIds.contains(String.valueOf(templateSeq))) {
                responseDo.setCode(StateCode.SYSTEM_CODE);
                responseDo.setInfo(StateCode.SYSTEM_MSG);
                return responseDo;
            }
            if (StringUtils.isAnyEmpty(smsTemplateArgs)) {
                responseDo.setCode(StateCode.SMS_EMPTY_CODE);
                responseDo.setInfo(StateCode.SMS_EMPTY_MSG);
                return responseDo;
            }
            SmsTemplate sms = smsTemplateMapper.getSmsTemplate(templateSeq);
            logger.info("短信模板信息 SmsTemplate = " + JSONObject.toJSONString(sms));
            if (sms == null) {
                responseDo.setCode(StateCode.MSG_EMPTY_CODE);
                responseDo.setInfo(StateCode.MSG_EMPTY_MSG);
                return responseDo;
            }
            String message = String.format(sms.getContent(), (String[]) smsTemplateArgs);
            boolean flag=WuXunSmsUtil.send(message, phone, 2);
            if(flag) {
                logger.info("短信发送成功phone="+phone+",message="+message+"");
                responseDo.setCode(StateCode.SUCCESS_CODE);
                responseDo.setInfo(StateCode.SUCCESS_MSG);
                responseDo.setData(message);
            }else{
                logger.info("短信发送失败phone="+phone+",message="+message+"");
                responseDo.setCode(StateCode.SYSTEM_CODE);
                responseDo.setInfo(StateCode.SYSTEM_MSG);
            }
        }catch(Exception e){
            responseDo.setCode(StateCode.SYSTEM_CODE);
            responseDo.setInfo(StateCode.SYSTEM_MSG);
            return responseDo;
        }
        return responseDo;
    }
}
