package com.jhh.jhs.loan.entity.manager;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity @Table(name = "loan_company_order") @Setter @Getter
public class LoanCompanyOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer orderId;
    private Integer companyId;
    private String createUser;
    private Date createDate;

}
