package com.jhh.jhs.loan.api.loan;

import com.jhh.jhs.loan.entity.app.NoteResult;

public interface YsbpayService {

	/**认证支付
	 * @param userId
	 * @param amount
	 * @param contract_id
	 * @param bank_id
	 * @param cardNo
	 * @param token
	 * @return
	 */
	public String ysbPayment(String userId, String amount, String contract_id,
                             String bank_id, String cardNo, String token);

	/**
	 * 还款认证支付回调后台地址
	 *
	 * @param result_code
	 * @param result_msg
	 * @param amount
	 * @param orderId
	 * @param mac
	 * @return
	 * @throws Exception
	 */
	public String callbackBackground(String result_code, String result_msg,
                                     String amount, String orderId, String mac) throws Exception;

	public String callbackBackgroundByNet(String orderJson) throws Exception;

	/**发起放款
	 * @param per_id
	 * @param borrId
	 * @return
	 * @throws Exception
	 */
	public String payCont(String per_id, String borrId) throws Exception;

	/**放款回调
	 * @param result_code
	 * @param result_msg
	 * @param amount
	 * @param orderId
	 * @param mac
	 * @return
	 * @throws Exception
	 */
	public String payContCallBack(String result_code, String result_msg,
                                  String amount, String orderId, String mac) throws Exception;

	/**
	 * 定时任务查询处理中的订单
	 */
	public void queryCall();

	/**定时查询还款处理中的订单
	 * @return
	 */
	public String queryPayment();

	/**用户在APP端主动还款提交
	 * @param per_id
	 * @param serial
	 * @param amount
	 * @return
	 */
	public NoteResult AppRepay(String per_id, String serial, String amount);

    public void queryQ();

    public int settlement(String orderId, String status, String msg);

	/**所有还款、代扣统一验证接口
	 * @param borrId
	 * @param thisAmount
	 * @return
	 */
	public NoteResult canPayCollect(String borrId, double thisAmount);

	/**第三方操作失败进行的数据库操作
	 * @param orderId
	 * @param result_msg
	 */
	public void fileCaozuo(String orderId, String result_msg);

	/**主动还款失败统一方法
	 * @param orderId
	 * @param desc
	 */
	public void paymentFail(String orderId, String desc);

}
