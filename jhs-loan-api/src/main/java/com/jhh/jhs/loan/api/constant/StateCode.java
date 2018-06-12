package com.jhh.jhs.loan.api.constant;

/**
 * 2017/12/29.
 */
public class StateCode {
    /**状态码*/
    public static final int SUCCESS_CODE = 200;
    public static final String SUCCESS_MSG = "操作成功";
    public static final int BLACKLIST_CODE = 2001;
    public static final String BLACKLIST_CODE_MSG = "黑名单";
    public static final int WHITELIST_CODE = 2002;
    public static final String WHITELIST_CODE_MSG = "白名单";
    /**-------------------------------错误码 -------------------------------------*/
    public static final int SYSTEM_CODE = 9999;
    public static final String SYSTEM_MSG = "系统繁忙";
    /**-----------------业务相关错误码-------------------------------------------*/
    public static final int PARAM_EMPTY_CODE = 1000;
    public static final String PARAM_EMPTY_MSG = "有必要参数为空";
    public static final int PHONE_NOTREGISTER_CODE = 1001;
    public static final String PHONE_NOTREGISTER_MSG = "该手机号未注册或被禁用";
    public static final int PHONE_REGISTER_CODE = 1002;
    public static final String PHONE_REGISTER_MSG = "注册失败，该手机号已经注册！";
    public static final int USERORPWD_ERROR_CODE = 1003;
    public static final String USERORPWD_ERROR_MSG = "用户名或密码不正确";
    public static final int UPDATEPWD_ERROR_CODE = 1004;
    public static final String UPDATEPWD_ERROR_MSG = "密码修改失败";
    public static final int OLDPWD_ERROR_CODE = 1005;
    public static final String OLDPWD_ERROR_MSG = "原密码输入错误";
    public static final int TOKEN_ERROR_CODE = 1006;
    public static final String TOKEN_ERROR_MSG = "token验证失败";
    public static final int PERSONINFO_ERROR_CODE = 1007;
    public static final String PERSONINFO_ERROR_MSG = "个人资料为空";
    public static final int REPEAT_SUBMIT_CODE = 1008;
    public static final String REPEAT_SUBMIT_MSG = "请勿多次提交";
    public static final int SMS_ERROR_CODE = 1009;
    public static final String SMS_ERROR_MSG = "验证码输入错误或验证码已过期，请重新输入";
    public static final int SMS_EMPTY_CODE = 1010;
    public static final String SMS_EMPTY_MSG = "验证码不存在";
    public static final int MSG_FAILURE_CODE = 1011;
    public static final String MSG_FAILURE_MSG = "模板已失效";
    public static final int MSG_EMPTY_CODE = 1012;
    public static final String MSG_EMPTY_MSG = "模板不存在";
    public static final int NOTBIND_CARD_CODE = 1013;
    public static final String NOTBIND_CARD_MSG = "未绑定银行卡";
    public static final int CARD_ERROR_CODE = 1014;
    public static final String CARD_ERROR_MSG = "请上传本人身份证";
    public static final int CARDNUM_INVALID_CODE = 1015;
    public static final String CARDNUM_INVALID_MSG = "该身份证已认证，无法重复认证";
    public static final int CARDNUM_OVERDUE_CODE = 1016;
    public static final String CARDNUM_OVERDUE_MSG = "该身份证已过期";
    public static final int VIP_NOT_CODE = 1017;
    public static final String VIP_NOT_MSG = "对不起，您不是会员用户";
    public static final int VIP_OVERDUE_CODE = 1018;
    public static final String VIP_OVERDUE_MSG = "对不起，您的会员已到期";
    public static final int VIP_RECHARGE_CODE = 1019;
    public static final String VIP_RECHARGE_MSG = "充值中，请稍等";
    public static final int BANK_ERROR_CODE = 1020;
    public static final String BANK_ERROR_MSG = "银行卡号与公司预留信息不符";
    public static final int SUBCONTRACT_ERROR_CODE = 1021;
    public static final String SUBCONTRACT_ERROR_MSG = "子协议绑定失败";

    /**----------------------借款相关错误码----------------------------------*/
    public static final int LOANSTATUS_ERROR_CODE = 2001;
    public static final String LOANSTATUS_ERROR_MSG = "当前合同状态不正确";
    public static final int ORDER_REPEAT_CODE = 2002;
    public static final String ORDER_REPEAT_MSG = "1分钟内无法重复借款，请稍后";
    public static final int ORDER_UNFINISHED_CODE = 2003;
    public static final String ORDER_UNFINISHED_MSG = "有正在处理中的借款，不允许重复借款！";
    public static final int BORROWLIST_NORMAL_CODE = 2004;
    public static final String BORROWLIST_NORMAL_MSG = "借款正常结清，或为新用户";
    public static final int BORROWLIST_OUTSTANDING_CODE = 2005;
    public static final String BORROWLIST_OUTSTANDING_MSG = "有未结清的借款";
    public static final int BORROWLIST_WAIT_CODE = 2006;
    public static final String  BORROWLIST_WAIT_MSG = "等待放款";
    public static final int AUDIT_FAILED_CODE = 2007;
    public static final String  AUDIT_FAILED_MSG = "电审未通过";
    public static final int OTHER_FAILED_CODE = 2008;
    public static final String  OTHER_FAILED_MSG = "其他状态";

    /**----------------------风空相关------------------------------------*/
    public static final int  PERSONAL_AUTHENTICATION_CODE = 3001;
    public static final String  PERSONAL_AUTHENTICATION_MSG = "个人认证失败，请稍候再试";
    public static final int  NODE_ERROR_CODE = 3002;
    public static final String  NODE_ERROR_MSG = "节点更新失败";
    public static final int  CONTACT_ERROR_CODE = 3003;
    public static final String  CONTACT_ERROR_MSG = "通讯录请求异常";
    public static final int  BPM_UNDO_CODE = 3004;
    public static final String  BPM_UNDO_MSG = "未认证";
    public static final int  RISK_ERROR_CODE = 3999;

    public static final int ZHI_MA_NEED_AUTH = 3005;

    /**----------------------产品相关------------------------------------*/
    public static final int PRODUCT_EMPTY_CODE = 4001;
    public static final String  PRODUCT_EMPTY_MSG = "该产品不存在";
}
