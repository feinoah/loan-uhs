
package com.jhh.jhs.loan.api.manager;

import com.jhh.jhs.loan.entity.manager.*;
import com.jhh.jhs.loan.entity.manager_vo.FeedbackVo;
import com.jhh.jhs.loan.entity.manager_vo.MsgTemplateVo;
import com.jhh.jhs.loan.entity.manager_vo.QuestionVo;
import com.jhh.jhs.loan.entity.utils.ManagerResult;

import java.util.List;

/**
 *描述：
 *@author: Wanyan
 *@date： 日期：2016年10月18日 时间：下午2:51:11
 *@version 1.0
 */
public interface ManageInfoService {
	
	public List<CodeType> getCodeTypeList();
	public ManagerResult deleteCodeType(String idfordel);
	public ManagerResult insertCodeType(CodeType record);
	public ManagerResult UpdateCodeType(CodeType record);
	
	public List<CodeValue> getCodeValueListByCode(String code_type);
    public ManagerResult deleteCodeValue(String idfordel);
	public ManagerResult insertCodeValue(CodeValue record);
	public ManagerResult UpdateCodeValue(CodeValue record);
	
	public ManagerResult insertQuestion(Question record);
	public ManagerResult UpdateQuestion(Question record);
	public List<QuestionVo> getAllQuestionList();
	
	public ManagerResult insertMsg(Msg record);
	public ManagerResult UpdateMsg(Msg record);
	
	public ManagerResult insertMsgTemplate(MsgTemplate record);
	public ManagerResult UpdateMsgTemplate(MsgTemplate record);
	public List<MsgTemplateVo> getAllMsgTemplateList();
	
	public List<FeedbackVo> getFeedbackList();

	public ManagerResult insertSmsTemplate(SmsTemplate record);
	public ManagerResult UpdateSmsTemplate(SmsTemplate record);
	public List<SmsTemplate> getAllSmsTemplateList();
}
