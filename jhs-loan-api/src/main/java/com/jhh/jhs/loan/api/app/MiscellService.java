package com.jhh.jhs.loan.api.app;

import com.jhh.jhs.loan.entity.app.YmFeedback;


public interface MiscellService {
	
	public String getMessageByUserId(String userId);
	
	public String getMyBorrowList(String userId);
	
	public String commonProblem(String userId);
	
	public String feedback(YmFeedback feed);
	
	public String getPersonInfo(String userId);
}
