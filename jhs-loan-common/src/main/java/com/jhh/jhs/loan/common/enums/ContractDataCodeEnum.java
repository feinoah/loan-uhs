package com.jhh.jhs.loan.common.enums;

/**
 * 电子合同Data数据类型枚举
 * zhushuaifei
 */
public enum ContractDataCodeEnum {
    GET_DATA_FOUR_TERM_IOS("1","four_term_ios"),   //四期_IOS
    GET_DATA_FOUR_TERM_ANDROID("2","four_term_android"),   //四期_安卓
    GET_DATA_ONE_TERM_ANDROID("3","one_term_android"),   //期_安卓
    GET_DATA_ONE_TERM_IOS("4","one_term_ios"),   //一期_IOS
    ;

    private String code;
    private String desc;

    private ContractDataCodeEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static String getDescByCode(String code){
        for (ContractDataCodeEnum payChannelEnum: ContractDataCodeEnum.values()){
            if(payChannelEnum.code.equals(code)){
                return payChannelEnum.desc;
            }
        }
        return null;
    }
}
