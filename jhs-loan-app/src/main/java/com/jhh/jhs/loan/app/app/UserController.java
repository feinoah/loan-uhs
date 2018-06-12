package com.jhh.jhs.loan.app.app;

import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.app.BQSService;
import com.jhh.jhs.loan.api.app.RiskService;
import com.jhh.jhs.loan.api.app.UserService;
import com.jhh.jhs.loan.api.constant.StateCode;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.TradeVo;
import com.jhh.jhs.loan.api.loan.YsbpayService;
import com.jhh.jhs.loan.api.sms.SmsService;
import com.jhh.jhs.loan.app.common.constant.Admin;
import com.jhh.jhs.loan.common.constant.PayCenterChannelConstant;
import com.jhh.jhs.loan.common.enums.SmsTemplateEnum;
import com.jhh.jhs.loan.common.util.CodeReturn;
import com.jhh.jhs.loan.common.util.Detect;
import com.jhh.jhs.loan.common.util.MD5Util;
import com.jhh.jhs.loan.common.util.MailSender;
import com.jhh.jhs.loan.common.util.PropertiesReaderUtil;
import com.jhh.jhs.loan.common.util.RedisConst;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.entity.manager.CodeValue;
import com.jhh.jhs.loan.entity.manager.Feedback;
import com.jhh.jhs.loan.entity.utils.RepaymentDetails;
import io.github.yedaxia.apidocs.ApiDoc;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Random;

/**
 * 用户模块
 * @author xuepengfei
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private YsbpayService ysbpayService;

    @Autowired
    private BQSService bqsService;

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private SmsService smsService;

    @Autowired
    private RiskService riskService;

    /**
     * 登录接口
     *
     * @param request
     * @return
     */
    @ResponseBody
    @ApiDoc(Admin.class)
    @RequestMapping("/userLogin")
    public String userLogin(HttpServletRequest request) {

        String result = "";
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String tokenKey = request.getParameter("tokenKey");
        String device = request.getParameter("device");
        password = MD5Util.encodeToMd5(password);
//-------------------------------------白骑士------------------------------------------------

        if (bqsService.runBQS(phone, "", "", "login",tokenKey ,device)) {
            result = userService.userLogin(phone, password);
            return result;
        } else {
            JSONObject obj = new JSONObject();
            obj.put("code", CodeReturn.baiqishi);
            obj.put("info", "您的信用等级较低，请稍后再试！");
            return obj.toString();

        }
    }

    @ResponseBody
    @ApiDoc(Admin.class)
    @RequestMapping("/userLoginByAuthCode")
    public String userLoginByAuthCode(HttpServletRequest request) {

        String result = "";
        String phone = request.getParameter("phone");
        //验证码获取
        String authCode = request.getParameter("authCode");
        String tokenKey = request.getParameter("tokenKey");
        String device = request.getParameter("device");
        //password = MD5Util.encodeToMd5(password);

//-------------------------------------白骑士------------------------------------------------

        if (bqsService.runBQS(phone, "", "", "login",tokenKey ,device)) {
            result = userService.userLoginByAuthCode(phone, authCode);
            return result;
        } else {
            JSONObject obj = new JSONObject();
            obj.put("code", CodeReturn.baiqishi);
            obj.put("info", "您的信用等级较低，请稍后再试！");
            return obj.toString();

        }
    }

    /**
     * 校验邀请码是否有效
     */
    @ResponseBody
    @RequestMapping("/checkInviteCode")
    @ApiDoc(Admin.class)
    @ApiImplicitParam(name="inviteCode", value="邀请码", dataType="Integer")
    public String checkInviteCode(HttpServletRequest request) {
        String inviteCode = request.getParameter("inviteCode");
        if (StringUtils.isEmpty(inviteCode)) {
            JSONObject obj = new JSONObject();
            obj.put("code", CodeReturn.success);
            obj.put("info", "邀请码未填写！");
            obj.put("data", inviteCode);
            return obj.toString();
        }
        return userService.checkInviteCode(inviteCode);
    }

    /**
     * 注册接口
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/userRegister")
    @ApiDoc(Admin.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name="phone", value="手机号", required=true),
            @ApiImplicitParam(name="password", value="密码", required=true),
            @ApiImplicitParam(name="inviteCode", value="邀请码", dataType="Integer")
    })
    public String userRegister(HttpServletRequest request) {
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String inviteCode = request.getParameter("inviteCode");

        password = MD5Util.encodeToMd5(password);
        Person user = new Person();
        user.setPhone(phone);
        user.setPassword(password);
        user.setIsLogin(1);
        user.setSource("1");
        if (!StringUtils.isEmpty(inviteCode)) {
            user.setInviter(Integer.parseInt(inviteCode));
        }
        user.setCreateDate(new Date());
        return userService.userRegister(user);
    }

    /**
     * 发送验证码
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/getCode")
    @ApiDoc(Admin.class)
    public String getCode(HttpServletRequest request) {
        JSONObject obj = new JSONObject();
        String result = "";
        String phone = request.getParameter("phone");
        String event = request.getParameter("event");
        String tokenKey = request.getParameter("tokenKey");
        String device = request.getParameter("device");


        // 风控黑名单校验，拒绝业务在风控端处理，目前业务端不需要做
        if("register".equals(event) && !riskService.isBlack(phone,null)){
            logger.info("手机号码{}，获取验证码在风控黑名单中",phone);
            obj.put("code", CodeReturn.baiqishi);
            obj.put("info", "您的信用等级较低，请稍后再试！");
            obj.put("data", "r");
            return obj.toString();

        }

        // -----------------------------白骑士-----------------------------------------

        if (("register".equals(event) && !(bqsService.runBQS(phone, "", "", "register",tokenKey,device) && bqsService.runBQS(phone, "", "", "sendDynamic",tokenKey,device)))
                                        ||
            ("sendDynamic".equals(event) && !bqsService.runBQS(phone, "", "", "sendDynamic",tokenKey,device))) {
            obj.put("code", CodeReturn.baiqishi);
            obj.put("info", "您的信用等级较低，请稍后再试！");
            obj.put("data", "");
            return obj.toString();
        }

        // --------------------------------白骑士--------------------------------
        // 生成6位数的验证码
        Random random = new Random();

        String radomInt = "";

        for (int i = 0; i < 6; i++) {

            radomInt += random.nextInt(10);

        }
        logger.info("生成的验证码==" + radomInt);
        //把验证码存到redis中,并设置两分钟分钟过期时间
        jedisCluster.setex(phone+"_"+radomInt,2*60, "1");

        // 老悠米的短信接口，要加标题模版
        // 2017.4.19更新 短信send第三个参数 0-悠兔 ，1-悠米，2-吾老板
        ResponseDo rspDo=smsService.sendSms(SmsTemplateEnum.CHECK_CODE_REMIND.getCode(),phone,radomInt);
        //String remessage = SmsUtil.sendSms(SmsUtil.MGYZM_CODE, radomInt, phone);
        if (StateCode.SUCCESS_CODE==rspDo.getCode()) {
            logger.info("验证码发送成功！");
            obj.put("code", CodeReturn.success);
            obj.put("info", "发送成功");
            obj.put("data", radomInt);
        } else {
            logger.info("验证码发送失败！");
            obj.put("code", CodeReturn.fail);
            obj.put("info", "发送失败");
            obj.put("data", "");
        }
        result = obj.toString();
        return result;
    }

    /**
     * 发送付款验证码
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/getPayCode")
    @ApiDoc(Admin.class)
    public NoteResult getPayCode(HttpServletRequest request) {
        NoteResult noteResult = new NoteResult();
        String phone = request.getParameter("phone");
        String event = request.getParameter("event");
        String tokenKey = request.getParameter("tokenKey");
        String device = request.getParameter("device");
        String bankNum = request.getParameter("bankNum");


        // 风控黑名单校验，拒绝业务在风控端处理，目前业务端不需要做
        if("register".equals(event) && !riskService.isBlack(phone,null)){
            logger.info("手机号码{}，获取验证码在风控黑名单中",phone);
            noteResult.setCode(String.valueOf(CodeReturn.baiqishi));
            noteResult.setInfo("您的信用等级较低，请稍后再试！");
            noteResult.setData("r");
            return noteResult;

        }

        // -----------------------------白骑士-----------------------------------------

        if (("register".equals(event) && !(bqsService.runBQS(phone, "", "", "register",tokenKey,device) && bqsService.runBQS(phone, "", "", "sendDynamic",tokenKey,device)))
                ||
                ("sendDynamic".equals(event) && !bqsService.runBQS(phone, "", "", "sendDynamic",tokenKey,device))) {
            noteResult.setCode(String.valueOf(CodeReturn.baiqishi));
            noteResult.setInfo("您的信用等级较低，请稍后再试！");
            return noteResult;
        }

        // 判断当前默认付款渠道是否是合利宝快捷支付
        CodeValue codeValue = userService.selectDefaultRepayChannel();

        TradeVo tradeVo = new TradeVo();
        tradeVo.setBankNum(bankNum);
        // 查询该银行卡是否需要指定拉卡拉快捷支付
        //TODO:临时解决方案 根据用户银行卡选择渠道路由
        tradeVo = userService.chooseBankRouting(tradeVo);
        if(("0".equals(codeValue.getCodeCode()) && PayCenterChannelConstant.PAY_CHANNEL_HLB_QUICK.equals(codeValue.getMeaning())) || PayCenterChannelConstant.PAY_CHANNEL_HLB_QUICK.equals(tradeVo.getPayChannel())){
            //判断用户是否绑定合利宝快捷支付,如果没有绑定,获取快捷支付验证码
            noteResult = userService.queryBindAndSendMsg(phone,bankNum);
            if(!"666".equals(noteResult.getCode())){
                return noteResult;
            }
        }

        // --------------------------------白骑士--------------------------------
        // 生成6位数的验证码
        Random random = new Random();

        String radomInt = "";

        for (int i = 0; i < 6; i++) {

            radomInt += random.nextInt(10);

        }
        logger.info("本地短信生成的验证码==" + radomInt);
        //把验证码存到redis中,并设置两分钟分钟过期时间
        String valiCodeKey = new StringBuilder(RedisConst.VALIDATE_CODE).append(RedisConst.SEPARATOR).append(phone).toString();
        jedisCluster.setex(valiCodeKey,2*60, radomInt);

        // 老悠米的短信接口，要加标题模版
        // 2017.4.19更新 短信send第三个参数 0-悠兔 ，1-悠米，2-吾老板
        ResponseDo rspDo=smsService.sendSms(SmsTemplateEnum.CHECK_CODE_REMIND.getCode(),phone,radomInt);
        //String remessage = SmsUtil.sendSms(SmsUtil.MGYZM_CODE, radomInt, phone);
        if (StateCode.SUCCESS_CODE==rspDo.getCode()) {
            logger.info("验证码发送成功！");
            noteResult.setCode(String.valueOf(CodeReturn.success));
            noteResult.setInfo("发送成功");
            noteResult.setData(radomInt);
        } else {
            logger.info("验证码发送失败！");
            noteResult.setCode(String.valueOf(CodeReturn.fail));
            noteResult.setInfo("发送失败");
        }
        return noteResult;
    }


    /**
     * 渠道引流页面获取验证码接口
     * @param phone
     * @param tokenKey
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getCodeForSourceNew", method = RequestMethod.POST)
    @ApiDoc(Admin.class)
    public String getCodeForSourceNew(String phone, String tokenKey, HttpServletRequest request) {
        //打印请求参数
        logger.error("phone" + phone + "tokenKey" + tokenKey);
//		logger.error("getCodeForSource被调用，客户端系统版本："+request.getHeader("User-Agent")+",IP:"+request.getRemoteAddr()+",主机名："+request.getRemoteHost()+",端口"+request.getRemotePort());
        JSONObject obj = new JSONObject();
        String result = "";
        String result_user = userService.getPersonByPhone(phone);
        com.alibaba.fastjson.JSONObject phone_obj = (com.alibaba.fastjson.JSONObject) com.alibaba.fastjson.JSONObject.parse(result_user);
        if (phone_obj.getInteger("code") != 200) {
            obj.put("code", 999);
            obj.put("info", "该手机号已经注册!");
            obj.put("data", "");
        } else {

            // 风控黑名单校验，拒绝业务在风控端处理，目前业务端不需要做
            if(!riskService.isBlack(phone,null)){

                obj.put("code", CodeReturn.baiqishi);
                obj.put("info", "综合评分不足");
                obj.put("data", "r");

                return obj.toString();

            }

            if (!bqsService.runBQS(phone, "", "", "sendDynamic",tokenKey,"h5")) {
                logger.error("白骑士没有通过！");
                obj.put("code", "9999");
                obj.put("info", "系统安全问题");
                obj.put("data", "");
                return obj.toString();
            }
            logger.error("白骑士通过！");
            // 生成6位数的验证码
            Random random = new Random();
            String radomInt = "";
            for (int i = 0; i < 6; i++) {
                radomInt += random.nextInt(10);
            }
            logger.info("生成的验证码==" + radomInt);
            // 新悠米的短信接口，不需要加标题模版
            // boolean re = smsService.send("【悠米闪借】您的验证码是:"+radomInt, phone);
            // if(re){
            // obj.put("code", CodeReturn.success);
            // obj.put("info", "发送成功");
            // obj.put("data", radomInt);
            // }else{
            // obj.put("code", CodeReturn.fail);
            // obj.put("info", "发送失败");
            // obj.put("data", "");
            // }

            // 老悠米的短信接口，要加标题模版
            // 2017.4.19更新 短信send第三个参数 0-悠兔 ，1-悠米，2-吾老板
            ResponseDo rspDo=smsService.sendSms(SmsTemplateEnum.CHECK_CODE_REMIND.getCode(),phone,radomInt);
            //String remessage = SmsUtil.sendSms(SmsUtil.MGYZM_CODE, radomInt, phone);
            if (StateCode.SUCCESS_CODE==rspDo.getCode()) {
                logger.error("验证码发送成功！");
                obj.put("code", CodeReturn.success);
                obj.put("info", "发送成功!");
                obj.put("data", radomInt);
            } else {
                logger.error("验证码发送失败！");
                obj.put("code", CodeReturn.fail);
                obj.put("info", "发送失败!");
                obj.put("data", "");
            }
        }

        result = obj.toString();
        return result;
    }


    @RequestMapping(value = "/getCodeForSource", method = RequestMethod.POST)
    public String getCodeForSource(String phone, String tokenKey, HttpServletRequest request) {

        return "redirect: http://www.baidu.com";
    }

    /**
     * 渠道引流页面注册接口
     * @param phone
     * @param password
     * @param source
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/userRegisterForSoruse", method = RequestMethod.POST)
    @ApiDoc(Admin.class)
    public String userRegisterForSoruse(String phone, String password, String source) {

        if (StringUtils.isEmpty(source)) {
            JSONObject obj = new JSONObject();
            obj.put("code", CodeReturn.fail);
            obj.put("info", "没有渠道信息");
            return com.alibaba.fastjson.JSONObject.toJSONString(obj);
        }

        logger.info("phone" + phone + "password" + password + "source" + source);
        if (source.contains("&")) {
            int position = source.indexOf("&");
            source = source.substring(0, position);
        }
        if (source.contains("?")) {
            int position2 = source.indexOf("?");
            source = source.substring(0, position2);
        }
        String result = "";
        password = MD5Util.encodeToMd5(password);

        Person user = new Person();
        user.setPhone(phone);
        user.setPassword(password);
        user.setIsLogin(1);
        user.setCreateDate(new Date());
        if ("x".equals(source.substring(0, 1))) {
            user.setSource(source);
        } else {
            user.setInviter(Integer.parseInt(source));
            user.setSource("2");
        }
        result = userService.userRegister(user);
        return result;
    }

    /**
     * 找回密码
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/updatePassword")
    @ApiDoc(Admin.class)
    public String updatePassword(HttpServletRequest request) {
        String result = "";
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        password = MD5Util.encodeToMd5(password);
        Person user = new Person();
        user.setPhone(phone);
        user.setPassword(password);
        result = userService.updatePassword(user);
        return result;
    }

    /**
     * 查询个人资料
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/getPersonInfo")
    @ApiDoc(Admin.class)
    public String getPersonInfo(HttpServletRequest request) {
        String result = "";
        String userId = request.getParameter("per_id");
        result = userService.getPersonInfo(userId);
        return result;
    }

    /**
     * 查询手机号有没有注册过
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/getPersonByPhone")
    @ApiDoc(Admin.class)
    public String getPersonByPhone(HttpServletRequest request) {
        String result = "";
        String phone = request.getParameter("phone");
        result = userService.getPersonByPhone(phone);
        return result;
    }

    /**
     * 修改密码
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/personUpdatePassword")
    @ApiDoc(Admin.class)
    public String personUpdatePassword(HttpServletRequest request) {
        JSONObject obj = new JSONObject();
        String result = "";
        String userId = request.getParameter("per_id");
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String tokenKey = request.getParameter("tokenKey");
        String device = request.getParameter("device");

        String token = request.getParameter("token");
        int yan = userService.yanzhengtoken(userId, token);
        if (201 == yan) {
            obj.put("code", CodeReturn.TOKEN_WRONG);
            obj.put("info", "该帐号已在别的设备登录，请重新登录");
            result = obj.toString();
            return result;
        }
//-------------------------------------白骑士------------------------------------------------

        if (!userService.updatePasswordCanbaiqishi(tokenKey, "modify", userId,device)) {
			obj.put("code", CodeReturn.baiqishi);
			obj.put("info", "您的信用等级较低，请稍后再试！");
			obj.put("data", "");
			result = obj.toString();
        } else {
            oldPassword = MD5Util.encodeToMd5(oldPassword);
            newPassword = MD5Util.encodeToMd5(newPassword);
            Person per = new Person();
            per.setId(Integer.parseInt(userId));
            per.setPassword(newPassword);
            per.setOldPassword(oldPassword);
            result = userService.personUpdatePassword(per);
        }
        return result;
    }

    /**
     * 意见反馈
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/userFeedBack")
    @ApiDoc(Admin.class)
    public String userFeedBack(HttpServletRequest request) {
        String result = "";
        String userId = request.getParameter("per_id");
        String content = request.getParameter("content");

        JSONObject obj = new JSONObject();
        String token = request.getParameter("token");
        int yan = userService.yanzhengtoken(userId, token);
        if (201 == yan) {
            obj.put("code", CodeReturn.TOKEN_WRONG);
            obj.put("info", "该帐号已在别的设备登录，请重新登录");
            result = obj.toString();
            return result;
        }

        Feedback feed = new Feedback();
        feed.setPerId(Integer.parseInt(userId));
        feed.setContent(content);
        feed.setCreateTime(new Date());
        result = userService.userFeedBack(feed);
        return result;
    }

    /**
     * 常见问题
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/getQuestion")
    @ApiDoc(Admin.class)
    public String getQuestion(HttpServletRequest request) {
        String result = "";
        result = userService.getQuestion();
        return result;
    }

    /**
     * 消息列表
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/getMessageByUserId")
    @ApiDoc(Admin.class)
    public String getMessage(HttpServletRequest request) {
        String result = "";
        String userId = request.getParameter("per_id");
        String nowPage = request.getParameter("nowPage");
        String pageSize = request.getParameter("pageSize");

        JSONObject obj = new JSONObject();
        String token = request.getParameter("token");

        int yan = userService.yanzhengtoken(userId, token);
        if (201 == yan) {
            obj.put("code", CodeReturn.TOKEN_WRONG);
            obj.put("info", "该帐号已在别的设备登录，请重新登录");
            result = obj.toString();
            return result;
        }

        // System.out.println(userId);
        result = userService.getMessageByUserId(userId,
                Integer.parseInt(nowPage), Integer.parseInt(pageSize));
        return result;
    }

    /**
     * 通用发送站内信消息
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/setMessage")
    @ApiDoc(Admin.class)
    public String setMessage(HttpServletRequest request) {
        String result = "";
        String userId = request.getParameter("per_id");
        String templateId = request.getParameter("templateId");
        String params = request.getParameter("params");

        result = userService.setMessage(userId, templateId, params);
        return result;
    }

    /**
     * 获取我的历史借款记录
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/getMyBorrowList")
    @ApiDoc(Admin.class)
    public String getMyBorrowList(HttpServletRequest request) {
        String result = "";
        String userId = request.getParameter("per_id");
        String nowPage = request.getParameter("nowPage");
        String pageSize = request.getParameter("pageSize");

        result = userService.getMyBorrowList(userId, Integer.parseInt(nowPage),
                Integer.parseInt(pageSize));
        return result;
    }

    /**
     * 查看借款列表中单个的详情
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/getProdModeByBorrId")
    @ApiDoc(Admin.class)
    public String getProdModeByBorrId(HttpServletRequest request) {
        String borrId = request.getParameter("borrId");

        NoteResult result = userService.getProdModeByBorrId(borrId);
        return JSONObject.toJSONString(result);
    }

    /**
     * 消息未读变已读
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/updateMessageStatus")
    @ApiDoc(Admin.class)
    public String updateMessageStatus(HttpServletRequest request) {
        String result = "";
        String userId = request.getParameter("per_id");
        String messageId = request.getParameter("messageId");

        JSONObject obj = new JSONObject();
        String token = request.getParameter("token");

        int yan = userService.yanzhengtoken(userId, token);
        if (201 == yan) {
            obj.put("code", CodeReturn.TOKEN_WRONG);
            obj.put("info", "该帐号已在别的设备登录，请重新登录");
            result = obj.toString();
            return result;
        }

        result = userService.updateMessageStatus(userId, messageId);
        return result;
    }


    /**
     * 还款详情
     *
     * @param request
     * @return
     */
    @RequestMapping("/getRepaymentDetails")
    @ApiDoc(Admin.class)
    public String getRepaymentDetails(HttpServletRequest request) {
        String userId = request.getParameter("per_id");
        String borrId = request.getParameter("borr_id");
        String type = request.getParameter("type");
        ResponseDo<RepaymentDetails> repaymentDetails = userService.getRepaymentDetails(userId, borrId, type);
        request.setAttribute("repaymentDetails", repaymentDetails);
        return "channel/pay";
    }

    /**
     * 资金记录
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/perAccountLog")
    @ApiDoc(Admin.class)
    public String perAccountLog(HttpServletRequest request, int nowPage, int pageSize) {

        String userId = request.getParameter("per_id");
        String result = userService.perAccountLog(userId, nowPage, pageSize);
        return result;
    }

    /**
     * 手工同步白名单
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/syncWhiteList")
    @ApiDoc(Admin.class)
    public String syncWhiteList(HttpServletRequest request) {
        NoteResult result = new NoteResult(CodeReturn.SUCCESS_CODE, "手工同步白名单成功");
        userService.syncWhiteList();
        return com.alibaba.fastjson.JSONObject.toJSONString(result);
    }

    /**
     * 手工同步白名单
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/syncPhoneWhiteList")
    @ApiDoc(Admin.class)
    public String syncPhoneWhiteList(HttpServletRequest request) {
        NoteResult result = new NoteResult(CodeReturn.SUCCESS_CODE, "手工同步白名单手机号成功");
        userService.syncPhoneWhiteList();
        return com.alibaba.fastjson.JSONObject.toJSONString(result);
    }

    /**
     * 检测是否可以引导用户下载金狐贷
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/getWithdrawInformation")
    @ApiDoc(Admin.class)
    public String getWithdrawInformation(HttpServletRequest request) {
        NoteResult result = new NoteResult(CodeReturn.FAIL_CODE, "不可用的参数");

        String userId = request.getParameter("per_id");

        try {
            result = userService.getWithdrawInformation(Integer.parseInt(userId));
            com.alibaba.fastjson.JSONObject dataObject = (com.alibaba.fastjson.JSONObject) result.getData();
            if (dataObject != null && dataObject.containsKey("isAvailable") && Boolean.TRUE.equals(dataObject.getBoolean("isAvailable"))) {
                String jhdRegisterUrl = PropertiesReaderUtil.read("common", "jhd.register");
                dataObject.put("gotoUrl", jhdRegisterUrl);
            } else {
                dataObject.put("gotoUrl", "");
            }
        } catch (NumberFormatException e) {
            logger.warn("非法的用户ID", e);
            return com.alibaba.fastjson.JSONObject.toJSONString(result);
        } catch (Throwable e) {
            logger.error("检测用户是否可以下载使用金狐贷提款失败", e);
            return com.alibaba.fastjson.JSONObject.toJSONString(result);
        }

        return com.alibaba.fastjson.JSONObject.toJSONString(result);
    }

    /**
     * 取消订单
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/canOfOrder")
    public String canOfOrder(HttpServletRequest request, String orderId,
                             String token, String info) {
        String result = "";

        result = userService.canOfOrder(orderId, token, info);
        return result;
    }

    /**
     * 查询还款中的订单 手动触发定时任务
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryPayment")
    @ApiDoc(Admin.class)
    public String queryPayment() {
        String res = ysbpayService.queryPayment();
        return res;
    }

    /**
     * 用户协议查看
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/agreement")
    @ApiDoc(Admin.class)
    public String queryAgreement() {
        JSONObject obj = new JSONObject();
        obj.put("code", CodeReturn.success);
        obj.put("info", "成功");
        String fileString = "";
        try {
            InputStream is = getClass().getResourceAsStream("/../../agreement.html");
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));

            String szTemp;
            while ((szTemp = bis.readLine()) != null) {
                fileString += szTemp;
            }
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        obj.put("data", fileString);
        // System.out.println(obj.get("data"));
        return obj.toString();
    }

    @RequestMapping("/sendSinaMail")
    @ResponseBody
    public void sendSinaMail(@RequestParam(required = false) String host) {
        MailSender cn = new MailSender();
        if (!Detect.notEmpty(host)) {
            host = "smtp.sina.com";
        }
        String[] to = {"wanzezhong@jinhuhang.com.cn"};
        String[] copyto = {"330402499@qq.com", "chenzhen@jinhuhang.com.cn"};

        // 设置要发送附件的位置和标题
//		cn.setAffix("C:\\Users\\wanzezhong\\Desktop\\20170621.xls", "20170621.xls");
        cn.setAffix("/data/www/youmi/youmiapp/billingNotice/20170621.xls", "20170621.xls");

        cn.setAddress("jinhuhanghlwzx@sina.com", to, copyto, "通知还款短信号码", "号码推送");
        cn.send(host, "jinhuhanghlwzx@sina.com", "jhh123456");
    }

    @RequestMapping("/sendWangYiMail")
    @ResponseBody
    public void sendWangYiMail(@RequestParam(required = false) String host) {
        MailSender cn = new MailSender();
        if (!Detect.notEmpty(host)) {
            host = "smtp.163.com";
        }
        String[] to = {"wanzezhong@jinhuhang.com.cn"};
        String[] copyto = {"330402499@qq.com", "chenzhen@jinhuhang.com.cn"};

        // 设置要发送附件的位置和标题
//		cn.setAffix("C:\\Users\\wanzezhong\\Desktop\\20170621.xls", "20170621.xls");
        cn.setAffix("/data/www/youmi/youmiapp/billingNotice/20170621.xls", "20170621.xls");

        cn.setAddress("carl_wanzezhong@163.com", to, copyto, "通知还款短信号码", "号码推送");
        cn.send(host, "carl_wanzezhong@163.com", "wan123123");
    }

    @RequestMapping("/sendjhhMail")
    @ResponseBody
    public void sendjhhMail(@RequestParam(required = false) String host) {
        MailSender cn = new MailSender();
        if (!Detect.notEmpty(host)) {
            host = "mail.jinhuhang.com.cn";
        }
        String[] to = {"wanzezhong@jinhuhang.com.cn"};
        String[] copyto = {"330402499@qq.com", "chenzhen@jinhuhang.com.cn"};

        // 设置要发送附件的位置和标题
//		cn.setAffix("C:\\Users\\wanzezhong\\Desktop\\20170621.xls", "20170621.xls");
        cn.setAffix("/data/www/youmi/youmiapp/billingNotice/20170621.xls", "20170621.xls");

        cn.setAddress("wanzezhong@jinhuhang.com.cn", to, copyto, "通知还款短信号码", "号码推送");
        cn.send(host, "wanzezhong", "wan+123");
    }



    @RequestMapping("/checkBlack")
    @ResponseBody
    public String checkBlack(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return com.alibaba.fastjson.JSONObject.toJSONString(new NoteResult(CodeReturn.FAIL_CODE, "电话号码为空"));
        } else {
            return com.alibaba.fastjson.JSONObject.toJSONString(userService.checkBlack(phone));
        }
    }

    /**
     * 删除用户认证缓存
     * @param per_id
     * @return
     */
    @RequestMapping("/deleteRedis")
    @ResponseBody
    @ApiDoc(Admin.class)
    public String deleteRedis(String per_id) {
        if (StringUtils.isEmpty(per_id)) {
            return "per_id为空";
        } else {
            return userService.deleteRedis(per_id);
        }
    }

    /**
     * 客户申请手机号和银行预留手机号验证
     * @param per_id 当前登录的用户Id
     * @param phone 银行预留手机号
     * @return
     */
    @RequestMapping("/checkBankPrePhone")
    @ResponseBody
    @ApiDoc(Admin.class)
    public NoteResult checkBankPhone(String per_id,String phone){
        return userService.checkBankPhone(per_id,phone);
    }


    /**
     * 客户注册时，手机号码风控校验
     * @param phone 客户注册时填写的手机号
     */
    @RequestMapping("/checkRegisterReviewers/{phone}")
    @ResponseBody
    @ApiDoc(Admin.class)
    public NoteResult checkRegisterReviewers(@PathVariable("phone") String phone){
        return userService.checkRegisterReviewers(phone);
    }
}