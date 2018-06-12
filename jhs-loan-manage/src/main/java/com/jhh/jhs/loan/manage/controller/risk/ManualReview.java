package com.jhh.jhs.loan.manage.controller.risk;

import com.jhh.jhs.loan.entity.common.Constants;
import com.jhh.jhs.loan.manage.controller.BaseController;
import com.jhh.jhs.loan.manage.entity.Response;
import com.jhh.jhs.loan.manage.service.risk.ReviewService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/review")
public class ManualReview extends BaseController{

	@Autowired
	private ReviewService reviewService;

	// 人工审核通过
	@ResponseBody
	@RequestMapping(value = "/pass", method = RequestMethod.POST)
	public Response pass(@RequestParam(required = true) Integer borrId,
					   @RequestParam(required = true) String reason,
					   @RequestParam(required = true) String userNum) throws Exception {
		Response response = reviewService.saveManuallyReview(borrId, reason, userNum, Constants.OperationType.MANUALLY_PASS);
		return response;
	}

	// 人工审拒绝
	@ResponseBody
	@RequestMapping(value = "/reject", method = RequestMethod.POST)
	public Response reject(@RequestParam(required = true) Integer borrId,
						 @RequestParam(required = true) String userNum,
						 @RequestParam(required = true) String reason) throws Exception {
		Response response = reviewService.saveManuallyReview(borrId, reason, userNum, Constants.OperationType.MANUALLY_REJECT);

		return response;
	}

	// 人工审拉黑
	@ResponseBody
	@RequestMapping(value = "/black", method = RequestMethod.POST)
	public Response black(@RequestParam(required = true) Integer borrId,
						 @RequestParam(required = true) String userNum,
						 @RequestParam(required = true) String reason) throws Exception {
		Response response = reviewService.saveManuallyReview(borrId, reason, userNum, Constants.OperationType.MANUALLY_BLACK);

		return response;
	}

	//  已签约拒绝
	@ResponseBody
	@RequestMapping(value = "/contract/reject", method = RequestMethod.POST)
	public Response contractReject(@RequestParam(required = true) Integer borrId,
						  @RequestParam(required = true) String userNum,
						  @RequestParam(required = true) String reason) throws Exception {
		Response response = reviewService.saveManuallyReview(borrId, reason, userNum, Constants.OperationType.CONTRACT_REJECT);

		return response;
	}

	//  已签约拉黑
	@ResponseBody
	@RequestMapping(value = "/contract/black", method = RequestMethod.POST)
	public Response contractBlack(@RequestParam(required = true) Integer borrId,
								   @RequestParam(required = true) String userNum,
								   @RequestParam(required = true) String reason) throws Exception {
		Response response = reviewService.saveManuallyReview(borrId, reason, userNum, Constants.OperationType.CONTRACT_BLACK);

		return response;
	}

	//  洗白
	@ResponseBody
	@RequestMapping(value = "/contract/white", method = RequestMethod.POST)
	public Response contractWhite(@RequestParam(required = true) Integer borrId,
								  @RequestParam(required = true) String userNum,
								  @RequestParam(required = true) String reason) throws Exception {
		Response response = reviewService.saveManuallyReview(borrId, reason, userNum, Constants.OperationType.WHITE);

		return response;
	}

	//  转件
	@ResponseBody
	@RequestMapping(value = "/transfer", method = RequestMethod.POST)
	public Response transfer(@RequestParam(required = true) String borrIds,
								   @RequestParam(required = true) String userNum) throws Exception {
		return reviewService.transfer(borrIds, userNum);
	}

	//  放款
	@ResponseBody
	@RequestMapping(value = "/pay", method = RequestMethod.POST)
    public Response pay(@RequestParam Integer borrId, @RequestParam String userNum, @RequestParam(required = false) String payChannel) throws Exception {
        return reviewService.pay(borrId, userNum, payChannel);
    }
}
