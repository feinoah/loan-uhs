package com.jhh.jhs.loan.entity.app;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="person")
@Getter
@Setter
@ToString
public class Person implements Serializable {
    private static final long serialVersionUID = 734164289531433453L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    private String password;

    private String phone;

    private Date checkDate;

    private String phoneBusiness;

    private String phoneService;

    private Integer grade;

    private Integer isLogin;

    private String tokenId;

    private String source;

    private Integer inviter;

    private Date createDate;

    private Date updateDate;

    private String sync;

    private String isManual;

    private String description;

    private String cardNum;

    private String name;

    private String sex;

    private String nation;

    private Date birthday;

    private String address;

    private String office;

    private Date startDate;

    private Date endDate;

    private String cardPhotoz;

    private String cardPhotof;

    private String cardPhotod;

    private String cardPhotov;

    private String bankName;

    private String bankCard;

    private String contactUrl;

    private Integer contactNum;

    private Date contactDate;

    private BigDecimal balance;

    @Transient
    private String oldPassword;
    @Transient
    private String device;

    private Date loginTime;
}