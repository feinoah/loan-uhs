package com.jhh.jhs.loan.app.app;

import com.google.common.collect.Maps;
import com.jhh.jhs.loan.api.app.ShareService;
import com.jhh.jhs.loan.api.app.UserService;
import com.jhh.jhs.loan.app.common.constant.Admin;
import com.jhh.jhs.loan.common.util.MD5Util;
import com.jhh.jhs.loan.entity.app.NoteResult;
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

import java.util.Date;
import java.util.Map;

/**
 * 好友分享模块
 *
 * @author xingmin
 * @date 2018/3/7
 */
@RequestMapping(value = "/share")
@RestController
public class ShareController {
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;
    @Autowired
    private ShareService shareService;

    /**
     * 分享页面注册接口
     *
     * @param phone
     * @param password
     * @param source
     * @return
     */
    @RequestMapping(value = "/userRegister", method = RequestMethod.POST)
    @ApiDoc(Admin.class)
    public String userRegister(String phone, String password, String source) {
        LOG.info(String.format("phone:%s, password:%s, source:%s", phone, password, source));

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
     * 获取活动规则
     *
     * @return
     */
    @RequestMapping(value = "/rules", method = RequestMethod.POST)
    @ApiDoc(Admin.class)
    @ResponseBody
    public Object getRules() {
        NoteResult result = NoteResult.SUCCESS_RESPONSE();
        Map<String, Object> map = Maps.newConcurrentMap();
        map.put("rules", shareService.getRules());
        map.put("image", shareService.getShareProcessUrl());
        result.setData(map);
        return result;
    }

    /**
     * 好友邀请列表
     *
     * @param nowPage
     * @param pageSize
     * @param perId    用户ID
     * @param level
     * @return
     */
    @RequestMapping(value = "/inviter/info", method = RequestMethod.POST)
    @ResponseBody
    @ApiDoc(Admin.class)
    public NoteResult inviterList(String nowPage, String pageSize, String perId, String level) {
        if (StringUtils.isEmpty(level) || StringUtils.isEmpty(perId)) {
            LOG.info(String.format("参数错误, perId【%s】 level【%s】......", perId, level));
            return NoteResult.FAIL_RESPONSE("参数错误");
        }

        return shareService.inviterLevel(perId, level, nowPage, pageSize);
    }

    /**
     * 分享出去的数据信息
     */
    @RequestMapping(value = "/data", method = RequestMethod.POST)
    @ResponseBody
    @ApiDoc(Admin.class)
    public NoteResult shareUrlData(String perId) {
        NoteResult result = NoteResult.SUCCESS_RESPONSE();
        result.setData(shareService.shareUrlData(perId));
        return result;
    }

}
