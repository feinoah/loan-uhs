package com.jhh.jhs.loan.app.app;

import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.app.BQSService;
import com.jhh.jhs.loan.api.app.RiskService;
import com.jhh.jhs.loan.api.app.UserService;
import com.jhh.jhs.loan.api.constant.StateCode;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.notice.UserBehaviorNoticeService;
import com.jhh.jhs.loan.api.sms.SmsService;
import com.jhh.jhs.loan.app.common.constant.Admin;
import com.jhh.jhs.loan.app.common.util.ValidateCode;
import com.jhh.jhs.loan.common.enums.SmsTemplateEnum;
import com.jhh.jhs.loan.common.util.CodeReturn;
import com.jhh.jhs.loan.common.util.Detect;
import com.jhh.jhs.loan.common.util.MD5Util;
import com.jhh.jhs.loan.entity.app.Person;
import io.github.yedaxia.apidocs.ApiDoc;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * 渠道模块
 * @author xingmin
 */
@RequestMapping(value = "/mark")
@RestController
public class MarkingController {
    private static final Logger logger = LoggerFactory.getLogger(MarkingController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private BQSService bqsService;
    @Autowired
    private SmsService smsService;

    @Autowired
    private RiskService riskService;
    @Autowired
    private UserBehaviorNoticeService userBehaviorNoticeService;
    /**
     * 响应验证码页面
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/validateCode")
    @ApiDoc(Admin.class)
    public String validateCode(HttpServletRequest request, HttpServletResponse response) throws Exception{
        // 设置响应的类型格式为图片格式
        response.setContentType("image/jpeg");
        //禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        String rnd=request.getParameter("rnd");
        ValidateCode vCode = new ValidateCode(120,40,4,50);
        String count=userService.setRedisData("h5VerifyCode"+rnd,2*60, vCode.getCode());
        vCode.write(response.getOutputStream());
        return null;
    }

    /**
     * 创建唯一标识到白骑士
     */
    @ResponseBody
    @RequestMapping(value="/createUUID")
    @ApiDoc(Admin.class)
    public String createUUID(HttpServletRequest request, HttpServletResponse response)throws Exception{
        JSONObject obj = new JSONObject();
        obj.put("msg", UUID.randomUUID().toString());
        return JSONObject.toJSONString(obj);
    }

    /**
     * 验证码后台校验
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/verifyCode")
    @ApiDoc(Admin.class)
    public String validateCode(HttpServletRequest request) {
        JSONObject obj = new JSONObject();
        String code = request.getParameter("code");
        String rnd=  request.getParameter("rnd");
        String sessionCode =userService.queryRedisData("h5VerifyCode"+rnd);
        if (!StringUtils.equalsIgnoreCase(code, sessionCode)) {
            obj.put("code", "201");
            obj.put("msg","图形验证码错误");
        }else{
            obj.put("code", "200");
            obj.put("msg","图形验证码正确");
        }
        return JSONObject.toJSONString(obj);
    }

    /**
     * 手机验证码后台发送
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/sendMsgCode")
    @ApiDoc(Admin.class)
    public String sendMsgCode(HttpServletRequest request) {
        JSONObject obj = new JSONObject();
        String phone = request.getParameter("phone");
        //验证用户是否已注册
        String result = "";
        String result_user = userService.getPersonByPhone(phone);
        com.alibaba.fastjson.JSONObject phone_obj = (com.alibaba.fastjson.JSONObject) com.alibaba.fastjson.JSONObject.parse(result_user);
        if (phone_obj.getInteger("code") != 200) {
            obj.put("code", 999);
            obj.put("info", "该手机号已经注册!");
            return obj.toString();
        }else{
            obj.put("code", 200);
            obj.put("info", "");
            return obj.toString();
        }
        //验证白骑士是否通过
//        boolean flag=false;
//        flag=bqsService.runBQS(phone, "", "", "sendDynamic", tokenKey,"h5");
//        if (flag!=true) {
//            logger.error("白骑士没有通过！");
//            obj.put("code", "9999");
//            obj.put("info", "拒绝注册，请稍后再试！");
//            obj.put("data", "");
//            return obj.toString();
//        }
//        logger.error("白骑士通过！");


//        HttpSession session = request.getSession();
//        // 生成6位数的验证码
//        Random random = new Random();
//        String radomInt = "";
//        for (int i = 0; i < 6; i++) {
//            radomInt += random.nextInt(10);
//        }
       // logger.info("生成的验证码==" + radomInt);
        // 老悠米的短信接口，要加标题模版
        // 2017.4.19更新 短信send第三个参数 0-悠兔 ，1-悠米，2-吾老板
       // String remessage = SmsUtil.sendSms(SmsUtil.MGYZM_CODE, radomInt, phone);
//        ResponseDo rspDo=smsService.sendSms(SmsTemplateEnum.CHECK_CODE_REMIND.getCode(),phone,radomInt);
//        if (StateCode.SUCCESS_CODE==rspDo.getCode()) {
//            logger.info("验证码发送成功！");
//            obj.put("code", CodeReturn.success);
//            obj.put("info", "发送成功");
//            session.setAttribute("msgCode",radomInt);
//        } else {
//            logger.info("验证码发送失败！");
//            obj.put("code", CodeReturn.fail);
//            obj.put("info", "发送失败");
//        }
    }
    /**
     * 短信验证码发送
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/sendSms")
    @ApiDoc(Admin.class)
    public String sendSms(HttpServletRequest request) {
        JSONObject obj = new JSONObject();
        String phone = request.getParameter("phone");
        // 生成6位数的验证码
        Random random = new Random();
        String radomInt = "";
        for (int i = 0; i < 6; i++) {
            radomInt += random.nextInt(10);
        }
        logger.info("生成的验证码==" + radomInt);

        // 风控黑名单校验，拒绝业务在风控端处理，目前业务端不需要做
        if(!riskService.isBlack(phone,null)){
            logger.info("发送短信手机号{}，在风控黑名单中",phone);
            obj.put("code", CodeReturn.baiqishi);
            obj.put("info", "综合评分不足");
            obj.put("data", "r");
            return obj.toString();
        }

        ResponseDo rspDo=smsService.sendSms(SmsTemplateEnum.CHECK_CODE_REMIND.getCode(),phone,radomInt);
        if (StateCode.SUCCESS_CODE==rspDo.getCode()) {
            logger.info("验证码发送成功！");
            obj.put("code", CodeReturn.success);
            obj.put("info", "发送成功");
            userService.setRedisData("h5msgCode"+phone+"",2*60, radomInt);
            //  session.setAttribute("msgCode",radomInt);
        } else {
            logger.info("验证码发送失败！");
            obj.put("code", CodeReturn.fail);
            obj.put("info", "发送失败");
        }
        return JSONObject.toJSONString(obj);
    }
    /**
     * 短信验证码后台校验
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/verifySmsCode")
    @ApiDoc(Admin.class)
    public String verifySmsCode(HttpServletRequest request) {
        JSONObject obj = new JSONObject();
        String code = request.getParameter("code");
        String phone = request.getParameter("phone");
        String sessionCode =userService.queryRedisData("h5msgCode"+phone+"");
        if (!StringUtils.equalsIgnoreCase(code, sessionCode)) {
            obj.put("code", "201");
            obj.put("msg","短信验证码错误");
        }else{
            obj.put("code", "200");
            obj.put("msg","短信验证码正确");
        }
        return JSONObject.toJSONString(obj);
    }

    /**
     * 渠道引流页面注册接口
     * @param phone
     * @param password
     * @param source
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/userRegister", method = RequestMethod.POST)
    @ApiDoc(Admin.class)
    public String userRegister(String phone, String password, String source) {

        if (StringUtils.isEmpty(source)) {
            JSONObject obj = new JSONObject();
            obj.put("code", CodeReturn.fail);
            obj.put("info", "没有渠道信息");
            return com.alibaba.fastjson.JSONObject.toJSONString(obj);
        }

        logger.info("phone" + phone + "password" + password + "source" + source);

        // 风控黑名单校验，拒绝业务在风控端处理，目前业务端不需要做
        if(!riskService.isBlack(phone,null)){
            logger.info("注册手机号码{}，在风控黑名单",phone);
            JSONObject obj = new JSONObject();
            obj.put("code", CodeReturn.baiqishi);
            obj.put("info", "综合评分不足");
            obj.put("data", "r");
            return obj.toString();

        }

        if (source.contains("&")) {
            int position = source.indexOf("&");
            source = source.substring(0, position);
        }
        if (source.contains("?")) {
            int position2 = source.indexOf("?");
            source = source.substring(0, position2);
        }
        password = MD5Util.encodeToMd5(password);

        Person user = new Person();
        user.setPhone(phone);
        user.setPassword(password);
        user.setIsLogin(1);
        user.setCreateDate(new Date());
        //x+num开头即渠道信息  
        if (source.matches("\\d+")) {
            user.setInviter(Integer.parseInt(source));
            user.setSource("2");
        } else {
           user.setSource(source);
        }
        return userService.userRegister(user);
    }

    /**
     * 下载App按钮
     * @param phone
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/downApp")
    @ApiDoc(Admin.class)
    public void downApp(String phone) {
        //点击下载APP删除注册缓存，增加登录缓存
        if(Detect.notEmpty(phone)){
            userBehaviorNoticeService.delRegisterRedis(phone);
            userBehaviorNoticeService.addLoginRedis(phone);
        }

    }
}
