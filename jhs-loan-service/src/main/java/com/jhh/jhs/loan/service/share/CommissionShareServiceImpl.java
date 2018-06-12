package com.jhh.jhs.loan.service.share;

import com.alibaba.dubbo.config.annotation.Service;
import com.jhh.jhs.loan.api.share.CommissionShareService;
import com.jhh.jhs.loan.common.util.RedisConst;
import com.jhh.jhs.loan.entity.app.Bank;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.entity.enums.CommissionOrderStatusEnum;
import com.jhh.jhs.loan.entity.enums.CommissionReviewStatusEnum;
import com.jhh.jhs.loan.entity.share.*;
import com.jhh.jhs.loan.mapper.app.BankMapper;
import com.jhh.jhs.loan.mapper.app.PersonMapper;
import com.jhh.jhs.loan.mapper.share.CommissionOrderMapper;
import com.jhh.jhs.loan.mapper.share.CommissionReviewMapper;
import com.jhh.jhs.loan.mapper.share.CommissionRuleMapper;
import com.jhh.jhs.loan.mapper.share.CommissionSummaryMapper;
import com.jhh.jhs.loan.util.CodeValueUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by chenchao on 2018/3/14.
 */
@Service
public class CommissionShareServiceImpl implements CommissionShareService {

    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private CommissionSummaryMapper commissionSummaryMapper;
    @Autowired
    private CommissionReviewMapper commissionReviewMapper;
    @Autowired
    private CommissionRuleMapper commissionRuleMapper;
    @Autowired
    private CommissionOrderMapper commissionOrderMapper;
    @Autowired
    private BankMapper bankMapper;

    @Override
    public InviteInfo queryCommissionByPersonId(String personId) {
        CommissionSummary commissionSummary = commissionSummaryMapper.queryCommissionByPersonId(personId);
        if (commissionSummary == null) {
            return new InviteInfo(Integer.parseInt(personId), "", 0, "0.00","0.00");
        }
        // 比较器返回结果，大于等于其数据字典配置值，1可提现，0不可提现
        int result = commissionSummary.getCommissionBalance().compareTo(getCommissionMinimumAmount());
        InviteInfo inviteInfo = new InviteInfo();
        inviteInfo.setInviteId(commissionSummary.getPerId());
        inviteInfo.setCommissionTotal(String.format("%.2f", commissionSummary.getCommissionTotal()));
        inviteInfo.setCommissionBalance(String.format("%.2f", commissionSummary.getCommissionBalance()));
        inviteInfo.setCanWithdraw(result >= 0 ? 1 : 0);
        inviteInfo.setInviteUrl("");
        return inviteInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteResult commissionWithDraw(String personId) {
        Person person = personMapper.selectByPrimaryKey(Integer.parseInt(personId));
        if (person == null) {
            return NoteResult.FAIL_RESPONSE("用户不存在");
        }

        Bank bank = bankMapper.selectPrimayCardByPerId(personId);
        if (bank == null) {
            return new NoteResult("202","未查询到用户银行卡信息");
        }

        CommissionSummary commissionSummary = commissionSummaryMapper.queryCommissionByPersonId(personId);
        if (commissionSummary == null) {
            return NoteResult.FAIL_RESPONSE("未查询到用户佣金信息");
        }

        if (commissionSummary.getCommissionBalance().compareTo(getCommissionMinimumAmount()) < 0) {
            return NoteResult.FAIL_RESPONSE(String.format("最低提取额为%s元，请多多邀请哟", getCommissionMinimumAmount()));
        }

        List<CommissionReview> reviewList = commissionReviewMapper.selectByCondition(personId, Arrays.asList(CommissionReviewStatusEnum.NO_REVIEW.getStatusString(),
                CommissionReviewStatusEnum.AGREE_REVIEW.getStatusString(),
                CommissionReviewStatusEnum.AGREE_BUT_PAY_FAIL.getStatusString()));
        if (reviewList.size() > 0) {
            return NoteResult.FAIL_RESPONSE("您有一笔待审核的佣金申请在处理中,请在这笔处理完成后再申请,谢谢!");
        }

        // 查询该用户可提现order详情
        CommissionOrder commissionOrder = new CommissionOrder();
        commissionOrder.setWithdrawStatus(0);
        commissionOrder.setMaxCreateDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        commissionOrder.setInviterId(personId);
        List<CommissionOrder> orders = commissionOrderMapper.selectByConditions(commissionOrder);
        if (orders.size() < 1) {
            return NoteResult.FAIL_RESPONSE("未查询到用户可提现佣金流水");
        }

        String ids = "";
        BigDecimal amount = new BigDecimal(0);
        for (CommissionOrder order : orders) {
            ids = "".equals(ids) ? Integer.toString(order.getId()) : ids + "," + order.getId();
            amount = amount.add(order.getCommissionAmount());
        }

        // 查询该用户手机号是否属于渠道商
        Example example = new Example(CommissionRule.class);
        example.createCriteria().andEqualTo("channelPhone", person.getPhone());
        List<CommissionRule> commissionRules = commissionRuleMapper.selectByExample(example);

        CommissionReview commissionReview = new CommissionReview();
        commissionReview.setPerId(personId);
        commissionReview.setPhone(person.getPhone());
        commissionReview.setIsChannel(commissionRules.size() < 1 ? 2 : 1);
        commissionReview.setCommissionOrderIds(ids);
        commissionReview.setApplyAmount(amount);
        commissionReview.setApplyDate(new Date());
        commissionReview.setStatus(CommissionReviewStatusEnum.NO_REVIEW.getStatus());

        try {
            if (commissionReviewMapper.insert(commissionReview) < 1) {
                return NoteResult.FAIL_RESPONSE("提现失败,请稍后重试");
            }

            // 更新用户佣金未领余额为 0
            commissionSummary.setCommissionBalance(BigDecimal.ZERO);
            commissionSummaryMapper.updateByPrimaryKeySelective(commissionSummary);

            // 更新commission order 提现状态为 1 已申请
            commissionOrderMapper.updateCommissionOrderStatus(CommissionOrderStatusEnum.APPLIED.getStatusString(), ids.split(","));

            return NoteResult.SUCCESS_RESPONSE("佣金领取成功,后台审核成功后将发放值您绑定的主卡。若审核失败会返还至您的可领佣金余额中。");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取佣金提现最低金额
     *
     * @return BigDecimal 佣金提现最小金额
     */
    private BigDecimal getCommissionMinimumAmount() {
        String value = CodeValueUtil.getCodeValueFromRedis(RedisConst.COMMISSION_MINIMUM_AMOUNT, 30 * 24 * 60 * 60, "commission_minimum_amount");
        if (StringUtils.isEmpty(value)) {
            return new BigDecimal(0);
        }
        return new BigDecimal(value);
    }
}
