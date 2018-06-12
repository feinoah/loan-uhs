package com.jhh.jhs.loan.manage.service.impl;

import com.jhh.jhs.loan.api.app.RiskService;
import com.jhh.jhs.loan.api.channel.AgentChannelService;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.AgentPayRequest;
import com.jhh.jhs.loan.api.message.MessageService;
import com.jhh.jhs.loan.api.sms.SmsService;
import com.jhh.jhs.loan.common.util.CodeReturn;
import com.jhh.jhs.loan.entity.app.BankVo;
import com.jhh.jhs.loan.entity.app.BorrowList;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.entity.common.Constants;
import com.jhh.jhs.loan.entity.common.ResponseCode;
import com.jhh.jhs.loan.entity.enums.BorrowStatusEnum;
import com.jhh.jhs.loan.entity.manager.BankBan;
import com.jhh.jhs.loan.entity.manager.Review;
import com.jhh.jhs.loan.manage.entity.Response;
import com.jhh.jhs.loan.manage.mapper.*;
import com.jhh.jhs.loan.manage.service.risk.ReviewService;
import com.jhh.jhs.loan.manage.service.user.UserService;
import com.jhh.jhs.loan.manage.utils.Assertion;
import com.jhh.jhs.loan.manage.utils.Detect;
import com.jhh.pay.driver.pojo.BankBinVo;
import com.jhh.pay.driver.pojo.BankInfo;
import com.jhh.pay.driver.pojo.QueryResponse;
import com.jhh.pay.driver.service.TradeService;
import com.jinhuhang.risk.dto.QueryResultDto;
import com.jinhuhang.risk.service.impl.blacklist.BlacklistAPIClient;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Service
@Log4j
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private AgentChannelService agentChannelService;

    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserService userService;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private MessageService messageService;

    @Autowired
    private RiskService riskService;

    @Autowired
    private BlacklistAPIClient riskClient;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private BankInfoMapper bankInfoMapper;

    @Autowired
    private BankBanMapper bankBanMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = java.lang.Exception.class)
    public Response saveManuallyReview(Integer borroId, String reason, String userNum, Integer operationType) throws Exception {
        Assertion.isPositive(borroId, "合同Id不能为空");
        Assertion.isPositive(operationType, "操作类型不能为空");
        Response response = new Response().code(ResponseCode.FIAL).msg("操作失败");

        BorrowList bl = borrowListMapper.selectByPrimaryKey(borroId);
        if (bl != null) {
            if (Constants.OperationType.MANUALLY_PASS.equals(operationType)) {
                //审核通过
                bl.setBorrStatus(BorrowStatusEnum.WAIT_SIGN.getCode());
                borrowListMapper.updateByPrimaryKeySelective(bl);
                //更新审核记录
                saveReview(borroId, reason, userNum);
                response.code(ResponseCode.SUCCESS).msg("操作成功");
            } else if (Constants.OperationType.MANUALLY_REJECT.equals(operationType)) {
                //审核拒绝
                bl.setBorrStatus(BorrowStatusEnum.REJECT_AUDIT.getCode());
                borrowListMapper.updateByPrimaryKeySelective(bl);
                //更新审核记录
                saveReview(borroId, reason, userNum);
                response.code(ResponseCode.SUCCESS).msg("操作成功");
            } else if (Constants.OperationType.CONTRACT_REJECT.equals(operationType)) {
                //签约拒绝
                bl.setBorrStatus(BorrowStatusEnum.REJECT_AUTO_AUDIT.getCode());
                borrowListMapper.updateByPrimaryKeySelective(bl);
                //更新审核记录
                saveReview(borroId, reason, userNum);
                //发送站内信
                Person person = personMapper.selectByPrimaryKey(bl.getPerId());
                messageService.setMessage(String.valueOf(person.getId()),
                        "3", person.getName());
                //发送短信
                smsService.sendSms(100002, person.getPhone());

                response.code(ResponseCode.SUCCESS).msg("操作成功");
            } else if (Constants.OperationType.MANUALLY_BLACK.equals(operationType)) {
                //审核拉黑
                bl.setBorrStatus(BorrowStatusEnum.REJECT_AUDIT.getCode());
                borrowListMapper.updateByPrimaryKeySelective(bl);
                //更新审核记录
                saveReview(borroId, reason, userNum);
                //TODO ..拉黑接口
                response = userService.userBlockWhite(bl.getPerId(), userNum, "",
                        reason, Constants.UserBlockWhite.BLACK);
            } else if (Constants.OperationType.WHITE.equals(operationType)) {
                //TODO ..洗白接口
                response = userService.userBlockWhite(bl.getPerId(), userNum, "",
                        reason, Constants.UserBlockWhite.WHITE);
            } else if (Constants.OperationType.CONTRACT_BLACK.equals(operationType)) {
                //已签约拉黑
                bl.setBorrStatus(BorrowStatusEnum.REJECT_AUTO_AUDIT.getCode());
                borrowListMapper.updateByPrimaryKeySelective(bl);
                //更新审核记录
                saveReview(borroId, reason, userNum);
                //TODO ..拉黑接口

                response = userService.userBlockWhite(bl.getPerId(), userNum, "",
                        reason, Constants.UserBlockWhite.BLACK);
            }
        }
        return response;
    }

    @Override
    public Response saveReview(Integer borroId, String reason, String employNum) {
        Assertion.isPositive(borroId, "合同Id不能为空");
        Assertion.notEmpty(employNum, "操作人不能为空");
        Response response = new Response().code(ResponseCode.FIAL).msg("操作失败");

        Review review = new Review();
        review.setReviewType(Constants.ReviewType.MANUALLY_REVIEW);
        review.setBorrId(borroId);
        review = reviewMapper.selectOne(review);

        if (review != null) {
            if (review.getEmployNum().equals(employNum)) {
                //如果为同一个人操作直接更新
                review.setReason(reason);
                reviewMapper.updateByPrimaryKeySelective(review);
            } else {
                //不同人，更新历史,在插入审核记录
                review.setReviewType(Constants.ReviewType.MANUALLY_REVIEW_HISTORY);
                reviewMapper.updateByPrimaryKeySelective(review);

                review.setId(null);
                review.setReviewType(Constants.ReviewType.MANUALLY_REVIEW);
                review.setReason(reason);
                review.setEmployNum(employNum);
                review.setCreateDate(Calendar.getInstance().getTime());
                reviewMapper.insertSelective(review);
            }
            response.code(ResponseCode.SUCCESS).msg("操作成功");
        } else {
            //没分过单的合同直接入库
            review = new Review();
            review.setBorrId(borroId);
            review.setReviewType(Constants.ReviewType.MANUALLY_REVIEW);
            review.setReason(reason);
            review.setEmployNum(employNum);
            review.setCreateDate(Calendar.getInstance().getTime());
            reviewMapper.insertSelective(review);
            response.code(ResponseCode.SUCCESS).msg("操作成功");
        }
        return response;
    }

    @Override
    public Response transfer(String brroIds, String userNum) {
        Response response = new Response().code(ResponseCode.FIAL).msg("操作失败");

        if (Detect.notEmpty(brroIds) && Detect.notEmpty(brroIds)) {
            String[] ids = brroIds.split(",");
            for (String id : ids) {
                saveReview(Integer.valueOf(id), "", userNum);
            }
            response.code(ResponseCode.SUCCESS).msg("操作成功");
        }
        return response;
    }

    @Override
    //FIXME
    public Response pay(Integer borrId, String userNum, String payChannel) {
        Assertion.isPositive(borrId, "合同号不能为空");
        Assertion.notEmpty(userNum, "审核人不能为空");
        Response response = new Response().code(ResponseCode.FIAL).msg("放款失败");

        BorrowList bl = borrowListMapper.selectByPrimaryKey(borrId);
        Assertion.notNull(bl, "合同不存在");

        // 校验用户认证节点状态
//        NoteResult note = riskService.checkBpm(Integer.toString(bl.getPerId()));
//        if (!CodeReturn.BPM_FINISH_CODE.equals(note.getCode())) {
//            response.setMsg("用户节点状态不正确【"+ note.getInfo() +"】");
//            return response;
//        }

        // 判断该用户是否是黑名单用户，如果是黑名单用户，提示“该用户是黑名单用户”，把该用户的状态改成“电审未通过”
        try {
            Map<String,String> customerInfo = personMapper.getCardNumAndPhoneByBorrId(borrId);
            QueryResultDto queryResultDto = riskClient.blacklistSingleQuery(customerInfo.get("cardNum"),customerInfo.get("phone"));
            if("0".equals(queryResultDto.getCode())){
                response.setMsg("该用户为黑名单用户, 放款失败");
                borrowListMapper.updateStatusById(borrId, BorrowStatusEnum.REJECT_AUTO_AUDIT.getCode());
                return response;
            }
        } catch (Exception e) {
            log.error("调用黑名单接口失败:" + ExceptionUtils.getFullStackTrace(e));
        }

        //合同状态为签约和失败的可以放款
        if (bl.getBorrStatus().equals(BorrowStatusEnum.SIGNED.getCode()) ||
                bl.getBorrStatus().equals(BorrowStatusEnum.LOAN_FAIL.getCode())) {
            AgentPayRequest request = new AgentPayRequest(bl.getPerId(), bl.getId() + "", 1, payChannel);
            //判断该用户银行卡是否可用
            BankVo bankVo = bankInfoMapper.selectMainBankByUserId(bl.getPerId());
            BankInfo bankInfo = assemblingParam(bankVo);
            QueryResponse<BankBinVo> bankBin = tradeService.getBankBin(bankInfo);
            log.info("查询用户银行卡卡bin返回结果 bankBin = \n"+bankBin);
            if (bankBin != null && "SUCCESS".equals(bankBin.getCode())) {
                Example queryExample = new Example(BankBan.class);
                Example.Criteria criteria = queryExample.createCriteria();
                criteria.andEqualTo("bankCode", bankBin.getData().getBankCode());
                criteria.andIn("type", Arrays.asList("1", "3"));
                List<BankBan> banBan = bankBanMapper.selectByExample(queryExample);
                if (banBan != null && banBan.size() > 0) {
                    response.setCode(201);
                    response.setMsg(bankBin.getData().getBankName()+"不允许放款，请提示客户换卡");
                    return response;
                }
            } else {
                response.setCode(201);
                response.setMsg(bankBin == null ? "验证银行卡失败" : bankBin.getMsg());
                return response;
            }
            ResponseDo<?> result = agentChannelService.pay(request);
            if (result != null) {
                response.setCode(result.getCode());
                response.setMsg(result.getInfo());
            }
        } else {
            response.msg("系统异常，合同状态不符，请刷新页面！");
        }
        return response;
    }

    private BankInfo assemblingParam(BankVo bankVo) {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setBankCard(bankVo.getBankNum());
        return bankInfo;
    }
}