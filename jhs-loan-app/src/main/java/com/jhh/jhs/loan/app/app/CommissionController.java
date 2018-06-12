package com.jhh.jhs.loan.app.app;

import com.jhh.jhs.loan.api.share.CommissionShareService;
import com.jhh.jhs.loan.app.common.constant.Admin;
import com.jhh.jhs.loan.common.util.CodeReturn;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.share.InviteInfo;
import io.github.yedaxia.apidocs.ApiDoc;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 佣金模块
 * @author xingmin
 */
@RequestMapping(value = "/commission")
@RestController
public class CommissionController {
    private Logger LOG = LoggerFactory.getLogger(CommissionController.class);

    @Autowired
    private CommissionShareService commissionShareService;

    /**
     * 查询佣金信息
     * @param perId 用户ID
     * @return
     */
    @RequestMapping(value = "/info", method = RequestMethod.POST)
    @ResponseBody
    @ApiDoc(Admin.class)
    public NoteResult myCommission(@RequestParam(value="perId" ,required = true) String perId) {
        NoteResult result = new NoteResult(CodeReturn.FAIL_CODE, "参数错误");
        if(StringUtils.isEmpty(perId)) {
            LOG.info("参数错误，perId is null........");
            return result;
        }

        InviteInfo inviteInfo = commissionShareService.queryCommissionByPersonId(perId);
        if(null == inviteInfo) {
            LOG.info(String.format("未查询到该用户【%s】相关的佣金信息........", perId));
            result.setInfo("未查询到该用户相关的佣金信息");
            return result;
        }

        result.setCode(CodeReturn.SUCCESS_CODE);
        result.setInfo("查询成功");
        result.setData(inviteInfo);
        return result;
    }

    /**
     * 佣金提现
     * @param perId 用户ID
     * @return NoteResult 200 成功  201 失败  202 未绑卡
     */
    @RequestMapping(value = "/withDraw", method = RequestMethod.POST)
    @ResponseBody
    @ApiDoc(Admin.class)
    public NoteResult commissionWithDraw(@RequestParam(value="perId" ,required = true) String perId) {
        if(StringUtils.isEmpty(perId)) {
            LOG.info("参数错误，perId is null........");
            return NoteResult.FAIL_RESPONSE("参数错误");
        }

        try {
            return commissionShareService.commissionWithDraw(perId);
        } catch (Exception e) {
            e.printStackTrace();
            return NoteResult.FAIL_RESPONSE("提现失败,请稍后再试");
        }

    }
}
