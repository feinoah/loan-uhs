package com.jhh.jhs.loan.entity.manager;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity @Table(name = "feedback") @Getter @Setter
public class Feedback implements Serializable{
    private Integer id;

    private Integer perId;

    private String content;

    private Date createTime;

    private String sync;
}