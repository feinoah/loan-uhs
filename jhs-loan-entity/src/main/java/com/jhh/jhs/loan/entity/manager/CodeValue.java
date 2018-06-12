package com.jhh.jhs.loan.entity.manager;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "code_value") @Setter @Getter
public class CodeValue implements Serializable{
    private Integer id;

    private String codeType;

    private String codeCode;

    private String meaning;

    private String description;

    /**
     * description与ios关键字冲突 先额外加个字段
     */
    @Transient
    private String descriptions;

    private String enabledFlag;

    private String sync;

    private Date updateDate;

}