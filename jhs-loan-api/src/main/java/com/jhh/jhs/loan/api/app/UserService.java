package com.jhh.jhs.loan.api.app;

import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.TradeVo;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.entity.manager.CodeValue;
import com.jhh.jhs.loan.entity.manager.Feedback;
import com.jhh.jhs.loan.entity.utils.RepaymentDetails;
import com.jhh.pay.driver.pojo.BindRequest;
import com.jhh.pay.driver.pojo.BindResponse;

public interface UserService {
	
	String userLogin(String phone, String password);
	//根据验证码登录
	String userLoginByAuthCode(String phone, String authCode);

	String userRegister(Person user);
	String updatePassword(Person user);
	String getPersonInfo(String userId);
	String getPersonByPhone(String phone);
	NoteResult getProdModeByBorrId(String borrId);
	String personUpdatePassword(Person user);
	String userFeedBack(Feedback feed);
	String getQuestion();
	String getMessageByUserId(String userId, int nowPage, int pageSize);
	String setMessage(String userId, String templateId, String params);
	String getMyBorrowList(String userId, int nowPage, int pageSize);
	String updateMessageStatus(String userId, String messageId);
	ResponseDo<RepaymentDetails> getRepaymentDetails(String userId, String borrId, String type);
	String perAccountLog(String userId, int nowPage, int pageSize);
	String canOfOrder(String orderId, String token, String info);
	boolean updatePasswordCanbaiqishi(String tokenKey, String event, String userId,String device);
	int yanzhengtoken(String userId, String token);
	NoteResult checkBlack(String phone);
	String deleteRedis(String per_id);

	NoteResult getWithdrawInformation(int userid);

	boolean isInWhiteList(String userid);

	void syncWhiteList();

	void syncPhoneWhiteList();
    String setRedisData(String key,int time,String data);
    String queryRedisData(String key);

	NoteResult checkBankPhone(String per_id,String phone);

	/**
	 * 注册时校验风控规则
	 * @param phone 注册填写的手机号
	 */
	NoteResult checkRegisterReviewers(String phone);

	/**
	 * 校验邀请码是否有效
	 * @param inviteCode
	 * @return
	 */
    String checkInviteCode(String inviteCode);

	/**
	 * 发送合利宝短信
	 *
	 * @param phone
	 * @param bankNum
	 * @return
	 */
    NoteResult sendHelipayMsg(String phone, String bankNum);

	/**
	 * 查询主动还款默认渠道信息
	 *
	 * @return
	 */
	CodeValue selectDefaultRepayChannel();

	/**
	 * 查询合利宝快捷支付绑卡
	 * @param bindRequest
	 * @return
	 */
	BindResponse queryBind(BindRequest bindRequest);

	/**
	 * 查询快捷支付绑定状态并发送验证码
	 * @param phone
	 * @param bankNum
	 * @return
	 */
	NoteResult queryBindAndSendMsg(String phone, String bankNum);

	/**
	 * 根据银行卡号查询快捷支付绑定状态
	 * @param bankNum
	 * @return
	 */
	Integer queryQuickBind(String bankNum);

	/**
	 * 选择路由渠道
	 * @param vo
	 */
	TradeVo chooseBankRouting(TradeVo vo);
}