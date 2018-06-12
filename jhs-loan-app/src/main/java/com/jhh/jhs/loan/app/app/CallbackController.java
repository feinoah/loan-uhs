package com.jhh.jhs.loan.app.app;

import com.jhh.jhs.loan.api.app.UserService;
import com.jhh.jhs.loan.api.channel.AgentBatchStateService;
import com.jhh.jhs.loan.api.loan.YsbpayService;
import com.jhh.jhs.loan.entity.callback.LKLBatchCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/callback")
public class CallbackController {

	@Autowired
	private UserService userService;

	@Autowired
	private YsbpayService ysbpayService;

	@Autowired
	private AgentBatchStateService agentBatchStateService;

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(CallbackController.class);

	/**
	 * 还款认证支付接口
	 *
	 *            用户ID
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/repayment")
	public String repayment(HttpServletRequest request) {
		String result = "";
		String per_id = request.getParameter("per_id");
		String amount = request.getParameter("amount");
		String conctact_id = request.getParameter("conctact_id");
		String bank_id = request.getParameter("bank_id");
		String cardNo = request.getParameter("cardNo");
		String tokenKey = request.getParameter("tokenKey");
		String token = request.getParameter("token");
		String device = request.getParameter("device");
		userService.updatePasswordCanbaiqishi(tokenKey, "repayment", per_id,device);
		result = ysbpayService.ysbPayment(per_id, amount, conctact_id, bank_id,
				cardNo, token);
		return result;
	}

	/**
	 * 支付中心批量代扣回调
	 * @param callback
	 */
	@RequestMapping("/batchDeduct")
	public void BatchDeductCallback(@RequestBody LKLBatchCallback callback){
		agentBatchStateService.batchCallback(callback);
	}
}
