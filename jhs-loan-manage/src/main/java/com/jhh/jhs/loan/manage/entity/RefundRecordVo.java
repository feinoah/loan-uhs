package com.jhh.jhs.loan.manage.entity;

import com.jhh.jhs.loan.entity.manager.BankList;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class RefundRecordVo {
    private String serialNo;
    private String channel;
    private String creationDate;
    private String updateDate;
    private String userName;
    private String idCard;
    private String phone;
    private String bankName;
    private String bankNum;
    private String amount ;
    private String remark;
}
