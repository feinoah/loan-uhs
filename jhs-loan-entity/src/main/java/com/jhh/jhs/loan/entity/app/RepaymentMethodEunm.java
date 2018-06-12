package com.jhh.jhs.loan.entity.app;

/**
 * Created by chenchao on 2018/1/5.
 */
public enum RepaymentMethodEunm {
    A("等额本息"), B("先息后本");

    private String repaymentMethod;

    private RepaymentMethodEunm(String repaymentMethod) {
        this.repaymentMethod = repaymentMethod;
    }

    public String getRepaymentMethod() {
        return repaymentMethod;
    }
}
