package com.jhh.jhs.loan.entity.loan;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "borrow_deductions")
@Getter @Setter
public class BorrowDeductions implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer borrId;
    private Integer perId;
    private Integer status;
    private String reason;
    private Date createDate;
    private Date updateDate;
}
