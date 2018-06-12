package com.jhh.jhs.loan.app.app;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.app.JuxinliService;
import com.jhh.jhs.loan.api.app.LoanService;
import com.jhh.jhs.loan.app.common.constant.Admin;
import com.jhh.jhs.loan.common.util.CodeReturn;
import com.jhh.jhs.loan.entity.Juxinli.ReqDtoBasicInfo;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.enums.JuxinliEnum;
import com.jhh.jhs.loan.entity.manager_vo.ReqBackPhoneCheckVo;
import io.github.yedaxia.apidocs.ApiDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;

/**
 * 聚信立模块
 * @author xuepengfei
 */
@Controller
@RequestMapping("/RC")
public class RcController {

    @Autowired
    private JuxinliService juxinliService;
    @Autowired
    private LoanService loanService;

    private static final Logger logger = LoggerFactory.getLogger(RcController.class);

    /**
     * 聚信立回调
     * @param request
     * @return
     */
    @ResponseBody
    @ApiDoc(Admin.class)
    @RequestMapping(value = "/jxlCallback", method = RequestMethod.POST)
    public String backPhoneCheckMessage(HttpServletRequest request) {

        String RCParam = request.getParameter("RCParam");
        ReqBackPhoneCheckVo callback = new ReqBackPhoneCheckVo();
        try {
            byte[] decodeBytes = java.util.Base64.getUrlDecoder().decode(URLDecoder.decode(RCParam, "UTF-8"));
            String decodeStr = new String(decodeBytes, "UTF-8");
            logger.info("jxlCallback:总参数RCParam：" + decodeStr);
            JSONObject json = JSONObject.parseObject(decodeStr);
            String model = json.getString("model");
            logger.info("jxlCallback:参数model:" + model);
            JSONObject req = JSONObject.parseObject(model);
            callback.setPhone(req.getString("phone"));
            callback.setDescription(json.getString("msg"));
            callback.setNode_status(json.getString("code"));
            callback.setRequestId(req.getString("requestId"));
        } catch (Exception e) {
            e.printStackTrace();
            return JSONObject.toJSONString(new NoteResult("2000", "参数错误"));
        }

        return JSONObject.toJSONString(juxinliService.backPhoneCheckMessage(callback));
    }

    /**
     * APP端发起聚信立认证请求
     * 通用接口 所有APP端有关手机认证的统一接口
     * 包括 提交服务密码 提交短信验证码 重新提交短信验证码
     * @param per_id 用户ID
     * @param command_code 请求code
     * @param phone_pwd 服务密码
     * @param query_code 查询密码
     * @param verify_code 验证码
     * @param token token
     * @return 接口JSON字符串
     */
    @ResponseBody
    @ApiDoc(Admin.class)
    @RequestMapping("/risk")
    public String risk(String per_id, String command_code, String phone_pwd, String query_code, String verify_code, String token) {
        NoteResult result = new NoteResult(JuxinliEnum.JXL_ERROR.getCode(), "参数错误");
        if (StringUtils.isEmpty(per_id) || StringUtils.isEmpty(token)) {
            return JSONObject.toJSONString(result);
        }
        command_code = StringUtils.isEmpty(command_code) ? "" : command_code;
        phone_pwd = StringUtils.isEmpty(phone_pwd) ? "" : phone_pwd;
        query_code = StringUtils.isEmpty(query_code) ? "" : query_code;
        verify_code = StringUtils.isEmpty(verify_code) ? "" : verify_code;
        logger.info(String.format("RCController-request:" + per_id + "," + command_code + "," + phone_pwd + "," + query_code + "," + verify_code + "," + token));
        String verify = loanService.verifyTokenId(per_id, token);
        if (CodeReturn.SUCCESS_CODE.equals(verify)) {
            ReqDtoBasicInfo reqDtoBasicInfo = new ReqDtoBasicInfo();
            reqDtoBasicInfo.setPer_id(per_id);
            reqDtoBasicInfo.setCode(command_code);
            reqDtoBasicInfo.setPassword(phone_pwd);
            reqDtoBasicInfo.setQueryPwd(query_code);
            reqDtoBasicInfo.setCaptcha(verify_code);

            result = juxinliService.risk(reqDtoBasicInfo);
        } else {
            result.setCode(CodeReturn.TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }
        logger.info(String.format("RCController-response: %s", result));
        return JSONObject.toJSONString(result);
    }

}
