package com.jhh.jhs.loan.manage.mapper;



import com.jhh.jhs.loan.entity.manager.Feedback;
import com.jhh.jhs.loan.entity.manager_vo.FeedbackVo;

import java.util.List;

public interface FeedbackMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Feedback record);

    int insertSelective(Feedback record);

    Feedback selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Feedback record);

    int updateByPrimaryKey(Feedback record);
    
    List<FeedbackVo> getFeedbackList();
}