package com.jhh.jhs.loan.app.app;

import com.jhh.jhs.loan.api.app.NotifyService;
import com.jhh.jhs.loan.app.common.constant.Admin;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.PersonNotify;
import com.jhh.jhs.loan.entity.enums.NotifyStatusEnum;
import com.jhh.jhs.loan.common.util.CodeReturn;
import com.alibaba.fastjson.JSONObject;
import io.github.yedaxia.apidocs.ApiDoc;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;

/**
 * 极光推送模块
 * @author chenchao
 */
@Controller
@RequestMapping("/notify")
public class NotifyController {

    private static final Logger logger = LoggerFactory.getLogger(NotifyController.class);

    @Autowired
    private NotifyService notifyService;

    /**
     * 极光注册绑定
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ApiDoc(Admin.class)
    public String registerPushId(HttpServletRequest request) {
        NoteResult result = new NoteResult(CodeReturn.FAIL_CODE, "不可用的参数");

        String userId = request.getParameter("per_id");
        String pushId = request.getParameter("push_id");

        if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(pushId)){
            logger.warn("无效的请求参数");
            return JSONObject.toJSONString(result);
        }

        PersonNotify personNotify = new PersonNotify();
        try {
            personNotify.setPerId(Integer.parseInt(userId));
            Date now = new Date(System.currentTimeMillis());
            personNotify.setUpdateDate(now);
            personNotify.setCreationDate(now);
            personNotify.setNotifyId(pushId);
            personNotify.setStatus(NotifyStatusEnum.Register.getStatusCode());
        } catch (NumberFormatException e) {
            logger.warn("非法的数字请求参数", e);
            return JSONObject.toJSONString(result);
        }
        result = notifyService.registerPersonNotify(personNotify);
        return JSONObject.toJSONString(result);
    }

    /**
     * 解除注册
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/unregister", method = RequestMethod.POST)
    @ApiDoc(Admin.class)
    public String unregisterPushId(HttpServletRequest request) {
        NoteResult result = new NoteResult(CodeReturn.FAIL_CODE, "不可用的参数");

        String userId = request.getParameter("per_id");
        String md5Sign = request.getParameter("md5sign");

        if(StringUtils.isEmpty(userId)){
            logger.warn("无效的请求参数");
            return JSONObject.toJSONString(result);
        }

        PersonNotify personNotify = new PersonNotify();
        try {
            personNotify.setPerId(Integer.parseInt(userId));
            Date now = new Date(System.currentTimeMillis());
            personNotify.setUpdateDate(now);
            personNotify.setCreationDate(now);
            personNotify.setStatus(NotifyStatusEnum.Unregister.getStatusCode());
        } catch (NumberFormatException e) {
            logger.warn("非法的数字请求参数", e);
            return JSONObject.toJSONString(result);
        }
        result = notifyService.unregisterPersonNotify(personNotify);
        return JSONObject.toJSONString(result);
    }
}
