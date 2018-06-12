package com.jhh.jhs.loan.app.app.capital;

public enum ResultStatusEnum {
    SUCCESS("0000", "成功"),
    PARAM_ERROR("0001", "参数错误"),
    SYSTEM_ERROR("0002", "系统错误"),
    USER_ID_NULL("0003", "传来的用户ID为空或者用户相关数据有错");

    private String code;

    private String message;

    private ResultStatusEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
