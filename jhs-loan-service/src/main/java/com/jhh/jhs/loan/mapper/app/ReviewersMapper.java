package com.jhh.jhs.loan.mapper.app;

import com.jhh.jhs.loan.entity.app.Reviewers;

import java.util.List;


public interface ReviewersMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Reviewers record);

    int insertSelective(Reviewers record);

    Reviewers selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Reviewers record);

    int updateByPrimaryKey(Reviewers record);
    
    //查询所有审核人的员工编号
    List<String> selectEmployNum();
}