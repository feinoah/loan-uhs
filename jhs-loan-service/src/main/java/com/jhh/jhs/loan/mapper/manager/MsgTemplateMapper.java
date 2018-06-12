package com.jhh.jhs.loan.mapper.manager;

import com.jhh.jhs.loan.entity.manager.MsgTemplate;
import com.jhh.jhs.loan.entity.manager_vo.MsgTemplateVo;

import java.util.List;

public interface MsgTemplateMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MsgTemplate record);

    int insertSelective(MsgTemplate record);

    MsgTemplate selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MsgTemplate record);

    int updateByPrimaryKeyWithBLOBs(MsgTemplate record);

    int updateByPrimaryKey(MsgTemplate record);
    
    List<MsgTemplateVo> getAllMsgTemplateList();
}