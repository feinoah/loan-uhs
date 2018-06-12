package com.jhh.jhs.loan.entity.app;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 产品表
 */
@Setter
@Getter
@Table(name = "product")
@Entity
@ToString
public class Product implements Serializable {

    private static final long serialVersionUID = 5846957251421587101L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String deviceFlag;

    private String productName;

    private String productDescription;

    private String productIcon;

    private String remark;

    private Integer term;

    private Integer termDay;

    private Float amount;

    private Float ransom;

    private Float rent;

    private Float penaltyRate;

    private Float deposit;

    private String repaymentMethod;

    private String status;

    private Date creationDate;

    private String creationUser;

    private Date updateDate;

    private String updateUser;

    private String sync;

    private Integer contractPrdouctId;

    private Float serviceAmount;

    public String getRepaymentMethod() {
        try {
            return RepaymentMethodEunm.valueOf(repaymentMethod).getRepaymentMethod();
        } catch (Exception e) {
            return null;
        }
    }
}
