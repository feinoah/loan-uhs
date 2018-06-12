package com.jhh.jhs.loan.entity.app_vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.var;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by xuepengfei on 2018/1/19.
 */
@Getter
@Setter
public class SignInfo implements Serializable {
    private String perId ;
    private String borrId ;
    private String bankNum ;
    private String amount ;
    private String termNum ;
    private String totalDay ;
    private String deposit ;
    private Integer rent ;
    private String device ;
    private String token;
    private BigDecimal serviceAmount; // 服务费
}
