package com.jhh.jhs.loan.manage.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter @Setter @Entity @Table(name = "riewer")
public class Riewer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String employNum;

    private String emplloyeeName;

    private String status;

    private Date creationDate;

    private Date updateDate;
}