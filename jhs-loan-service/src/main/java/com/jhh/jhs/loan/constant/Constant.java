package com.jhh.jhs.loan.constant;

/**
 * 常量类
 */
public class Constant {

    /**3代表现金分期*/
    public static final int SMS_TYPE = 3;
    /**1代表悠多多*/
    public static final  int SMS_YHS=4;
    /**借款用途 字典表中codeT*/
    public static final String PRODUCT_USE = "product_use";
    /*上传通讯录名称*/
    public static final String CONTACTSFILENAME = "jhs_con";

    //借款状态常量
    public static final String STATUS_APLLY = "BS001";//申请中
    public static final String STATUS_CANCEL = "BS007";//已取消
    public static final String STATUS_WAIT_SIGN = "BS002";//待签约
    public static final String STATUS_SIGNED = "BS003";//已签约
    public static final String STATUS_TO_REPAY = "BS004";//待还款
    public static final String STATUS_LATE_REPAY = "BS005";//逾期未还
    public static final String STATUS_PAY_BACK = "BS006";//已还清
    public static final String STATUS_REVIEW_FAIL = "BS008";//审核未通过
    public static final String STATUS_PHONE_REVIEW_FAIL = "BS009";//电审未通过
    public static final String STATUS_DELAY_PAYBACK = "BS010";//逾期还清
    public static final String STATUS_COM_PAYING = "BS011";//放款中
    public static final String STATUS_COM_PAY_FAIL = "BS012";//放款失败
    public static final String STATUS_PAYING = "BS013";// 还款中
    public static final String STATUS_EARLY_PAYBACK = "BS014";// 提前结清
    /**风控成功码*/
    public static final String SUCCESS_CODE = "0000";
    public static final String WAIT_STATUS = "p";
    public static final String SUCCESS_STATUS = "s";
    public static final String FAIL_STATUS = "f";

    /**业务支付渠道*/
    public static final String YSB_CHANNEL = "ysb";
    public static final String HAIER_CHANNEL = "haier";
    public static final String PAYCENTER_CHANNEL = "payCenter";
    /**支付中心开启 type*/
    public static final String PAYCENTER_CHANNEL_TYPE = "0";
    /**本地支付开启 type*/
    public static final String LOCAL_CHANNEL_TYPE = "1";

    public static final String PAY_SWITCH = "pay_style_pay";

    public static final String DEDUCT_SWITCH = "pay_style_deduct";

    public static final String REPAY_SWITCH = "pay_style_repay";
    public static final String REFUND_SWITCH = "pay_style_refund";


    /**无业务调用第三方*/
    public static final String YSB_TRADE_CODE = "tradeysb";
    public static final String HAIER_TRADE_CODE = "tradehaier";
    public static final String PAYCENTER_TRADE_CODE = "tradePayCenter";
}
