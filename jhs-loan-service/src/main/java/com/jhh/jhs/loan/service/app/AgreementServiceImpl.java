package com.jhh.jhs.loan.service.app;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jhh.jhs.loan.api.constant.StateCode;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.agreement.BorrowAgreement;
import com.jhh.jhs.loan.constant.Constant;
import com.jhh.jhs.loan.entity.app.BorrowList;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.entity.app.Private;
import com.jhh.jhs.loan.mapper.app.BorrowListMapper;
import com.jhh.jhs.loan.mapper.app.PersonMapper;
import com.jhh.jhs.loan.mapper.app.PrivateMapper;
import com.jinhuhang.settlement.dto.SettlementResult;
import com.jinhuhang.settlement.service.SettlementAPI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 2018/1/23.
 */
@Service
@Slf4j
public class AgreementServiceImpl {

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private SettlementAPI settlementAPI;

    @Autowired
    private PrivateMapper privateMapper;

    public ResponseDo<BorrowAgreement> getBorrowAgreement(String perId) {
        log.info("借款协议请求参数 perId = " + perId);
        if (StringUtils.isEmpty(perId)) {
            log.error(StateCode.PARAM_EMPTY_MSG);
            return ResponseDo.newFailedDo(StateCode.PARAM_EMPTY_MSG);
        }
        //获取借款用户信息
        Person person = personMapper.selectByPrimaryKey(Integer.parseInt(perId));
        if (person == null) {
            log.error(StateCode.PHONE_NOTREGISTER_MSG);
            return ResponseDo.newFailedDo(StateCode.PHONE_NOTREGISTER_MSG);
        }
        //获取当前合同信息
        BorrowList bl = borrowListMapper.selectNow(Integer.parseInt(perId));
        if (bl == null || !Constant.STATUS_WAIT_SIGN.equals(bl.getBorrStatus())) {
            log.error(StateCode.LOANSTATUS_ERROR_MSG);
            return ResponseDo.newFailedDo(StateCode.LOANSTATUS_ERROR_MSG);
        }

        // 合同总期数为1，不再调用试算接口
        if ( 1 == bl.getTotalTermNum()) {
            bl.setPlanRepay(bl.getBorrAmount() + bl.getPlanRental());
            return getResponse(person, bl, Lists.newArrayList());
        }

        // 调用清结算试算
        ResponseDo<SettlementResult> responseDo = getPlanInfo(bl.getId());
        if (200 != responseDo.getCode()) {
            return ResponseDo.newFailedDo("系统异常,请联系客服");
        }

        String model = responseDo.getData().getModel();
        if (StringUtils.isEmpty(model)) {
            return ResponseDo.newFailedDo("试算信息为空");
        }

        List<Map<String, Object>> settlements = JSONObject.parseObject(model, List.class);
        return getResponse(person, bl, settlements);
    }

    /**
     * 调用风控获取试算结果
     *
     * @param borrowId
     * @return
     */
    public ResponseDo<SettlementResult> getPlanInfo(int borrowId) {
        SettlementResult planInfo;
        try {
            planInfo = settlementAPI.getPlanInfo(borrowId);
        } catch (Exception e) {
            log.error("试算接口调用失败");
            return ResponseDo.newFailedDo("试算接口调用失败");
        }
        if (planInfo == null) {
            log.error("试算接返回值为空");
            return ResponseDo.newFailedDo("试算接返回值为空");
        } else {
            if (1 == planInfo.getCode()) {
                ResponseDo<SettlementResult> responseDo = ResponseDo.newSuccessDo();
                responseDo.setData(planInfo);
                return responseDo;
            } else {
                log.error("试算接口返回错误信息：" + planInfo.getMsg());
                return ResponseDo.newFailedDo(planInfo.getMsg());
            }
        }
    }

    private ResponseDo<BorrowAgreement> getResponse(Person person, BorrowList borrowList, List<Map<String, Object>> settlements) {
        ResponseDo<BorrowAgreement> result = ResponseDo.newSuccessDo();
        //获取用户EMAIL
        Private pr = privateMapper.selectByPerId(person.getId());
        BorrowAgreement agreement = new BorrowAgreement(person, borrowList, settlements);
        agreement.setEmail(pr.getEmail());
        result.setData(agreement);
        return result;
    }
}
