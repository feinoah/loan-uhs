package com.jhh.jhs.loan.dao;


import com.jhh.jhs.loan.model.Msg;


public interface MsgMapper {

    int insertSelective(Msg record);

}