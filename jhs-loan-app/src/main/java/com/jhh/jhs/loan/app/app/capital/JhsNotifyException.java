package com.jhh.jhs.loan.app.app.capital;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Copyright@上海翔川
 * Created by FireHole on 2017/6/14.
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class JhsNotifyException extends RuntimeException{

    private String code;
    private String desc;

    public JhsNotifyException(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
