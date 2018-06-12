package com.jhh.jhs.loan.entity.app_vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by zhangqi on 2016/10/12.
 */
@Getter
@Setter
public class PersonInfo implements Serializable{



    /**
     * bankName : 工商
     * bankNum : 9哦哦哦
     * cardNum : 320611199404071815
     * monthlypay : 未婚
     * name : 张琦
     * perId : 6000057
     * phone : 13270665702
     * profession : 高中
     * relaPhone : 15061978217
     * relatives : 高中
     * relativesName : 晨哥
     * sociPhone : 15061978217
     * society : 未婚
     * societyName : 晨哥
     */

    private String bankLocalId;
    private String bankName;
    private String bankNum;
    private String cardNum;
    private String monthlypay;
    private String name;
    private String perId;
    private String phone;
    private String profession;
    private String relaPhone;
    private String relatives;
    private String relativesName;
    private String houseCondition;
    private String isCar;
    private String email;
    private String society;
    private String societyName;
    private String sociPhone;

}
