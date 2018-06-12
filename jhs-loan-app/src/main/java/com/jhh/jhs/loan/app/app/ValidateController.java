package com.jhh.jhs.loan.app.app;

import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.app.VersionService;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.app.common.constant.Admin;
import com.jhh.jhs.loan.entity.app.Version;
import io.github.yedaxia.apidocs.ApiDoc;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;


/**
 * 版本更新模块
 *
 * @author OnionMac
 * @date 2018/1/26
 */
@Controller
@Log4j
public class ValidateController {

    @Autowired
    VersionService mVersionService;

    @ResponseBody
    @RequestMapping("/validate/**")
    @ApiDoc(Admin.class)
    public ResponseDo<Version> validate(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        System.out.println("validate 请求URI:" + requestUri);
        System.out.println("validate POST 方式请求参数:" + JSONObject.toJSON(request.getParameterMap()));

        if (!StringUtils.isEmpty(requestUri) && requestUri.indexOf("getVersion") != -1) {
            String clientName = request.getParameter("clientName");
            String versionName = request.getParameter("versionName");
            if (!StringUtils.isEmpty(clientName) && !StringUtils.isEmpty(versionName)) {
                return mVersionService.getVersionByVersionName(clientName, versionName);
            }
        }

        String agent = request.getHeader("user-agent");
        if (!StringUtils.isEmpty(agent)) {
            agent = agent.toLowerCase();
            if (agent.contains("ios") || agent.contains("iphone") || agent.contains("ipod") || agent.contains("ipad")) {
                return ResponseDo.newFailedDo("您当前的应用版本已停止服务，请升级到最新版本，谢谢您的配合！");
            } else {
                return ResponseDo.newFailedDo("您当前的应用版本已停止服务，请通过应用宝升级到最新版本，谢谢您的配合！");
            }
        }

        return ResponseDo.newFailedDo("您当前的应用版本已停止服务，请通过苹果市场或者应用宝升级到最新版本，谢谢您的配合！");
    }
}
