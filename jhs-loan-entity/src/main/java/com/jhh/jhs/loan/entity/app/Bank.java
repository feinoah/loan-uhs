package com.jhh.jhs.loan.entity.app;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "bank")
@Getter @Setter
public class Bank implements Serializable{
    @Id
    private Integer id;

    private Integer perId;

    private String bankId;

    public Bank(Integer perId, String bankId, String bankNum, String phone,String bankName) {
        this.perId = perId;
        this.bankId = bankId;
        this.bankNum = bankNum;
        this.phone = phone;
        this.bankName = bankName;
    }
    public Bank() {
    }

    private String bankNum;

    private String phone;

    private String status;

    private Date startDate;

    private Date endDate;

    private String resultCode;

    private String resultMsg;

    private String subContractNum;

    private Date creationDate;

    private Integer creationUser;

    private Date updateDate;

    private Integer updateUser;

    private String bankName;

    private String sync;

    private String bankCode;

    private Integer quickPayStatus;

}