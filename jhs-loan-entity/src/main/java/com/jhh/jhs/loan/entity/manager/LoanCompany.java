package com.jhh.jhs.loan.entity.manager;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 公司
 */
@Entity @Table(name = "loan_company")
@Setter @Getter
public class LoanCompany {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private Integer status;
}
