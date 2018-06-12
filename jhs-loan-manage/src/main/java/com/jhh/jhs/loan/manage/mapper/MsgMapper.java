package com.jhh.jhs.loan.manage.mapper;


import com.jhh.jhs.loan.entity.manager.Msg;

import java.util.List;

public interface MsgMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Msg record);

    int insertSelective(Msg record);

    Msg selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Msg record);

    int updateByPrimaryKeyWithBLOBs(Msg record);

    int updateByPrimaryKey(Msg record);
    
    List<Msg> getMessageByUserId(String id, int start, int pageSize);
    
    int updateMessageStatus(String msgId);
    
    int selectUnread(String perId);
}