package com.jhh.jhs.loan.entity.manager;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class RepaymentPlan implements Serializable{
    private Integer id;

    private Integer contractId;

    private String contractType;

    private Integer term;

    private Date repayDate;

    private Integer overdueDays;

    private BigDecimal rentalAmount;

    private BigDecimal penalty;

    private BigDecimal penaltyRate;

    private Integer status;

    private BigDecimal surplusRentalAmount;

    private BigDecimal surplusPenalty;

    private Integer extension;

    private Date creationDate;

    private Integer creationUser;

    private Date updateDate;

    private Integer updateUser;

    private Integer version;

    private String rundate;

    private Integer isRun;

    private Integer isLast;

    private BigDecimal paidAmount;

    private BigDecimal surplusAmount;

}