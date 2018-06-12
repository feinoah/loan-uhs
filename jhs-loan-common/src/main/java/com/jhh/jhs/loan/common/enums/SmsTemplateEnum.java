package com.jhh.jhs.loan.common.enums;

/**
 * 短信模板整理
 * zhushuaifei
 */
public enum SmsTemplateEnum {
    LOAN_SUCCESS_REMIND(500001,"放款成功"),
    RENT_REMIND(500002,"租金提醒"),
    OVERDUE_REMIND(500003,"逾期提醒"),
    REPAY_SUCCESS_REMIND(500004,"主动付款成功"),
    PAY_SUCCESS_REMIND(500005,"代扣成功"),
    CHECK_CODE_REMIND(500006,"验证码发送接口"),
    ALL_REPAY_REMIND(500007,"全部还清"),
    PAY_FAIL_REMIND(500008,"付款失败"),
    INIT_PASSWORD_REMIND(500009,"初始密码"),
    COMM_APPROVE_FAIL(500010,"佣金审核未通过"),
    REFUND_FAIL(500011,"财务退款失败"),
    ;


    private Integer code;
    private String desc;

    private SmsTemplateEnum(Integer code, String desc){
        this.code = code;
        this.desc = desc;
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDescByCode(String code){
        for (SmsTemplateEnum payChannelEnum: SmsTemplateEnum.values()){
            if(payChannelEnum.code.equals(code)){
                return payChannelEnum.desc;
            }
        }
        return null;
    }
}
