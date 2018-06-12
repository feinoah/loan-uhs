package com.jhh.jhs.loan.entity.app;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "reviewers")
public class Reviewers implements Serializable{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String employNum;

    private String emplloyeeName;

    private String status;

    private String creationDate;

    private String updateDate;
}