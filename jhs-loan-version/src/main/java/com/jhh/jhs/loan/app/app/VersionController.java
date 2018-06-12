package com.jhh.jhs.loan.app.app;

import com.jhh.jhs.loan.api.app.VersionService;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.entity.app.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 版本更新模块
 *
 * @author OnionMac
 * @date 2018/1/26
 */
@Controller
@RequestMapping("/version")
public class VersionController {

    @Autowired
    VersionService mVersionService;

    /**
     * 获取最新版本接口
     *
     * @param clientName  设备标识
     * @param versionName 当前版本
     * @return
     */
    @ResponseBody
    @RequestMapping("/getVersion")
    public ResponseDo<Version> getVersion(String clientName, String versionName) {

        return mVersionService.getVersionByVersionName(clientName, versionName);
    }
}
