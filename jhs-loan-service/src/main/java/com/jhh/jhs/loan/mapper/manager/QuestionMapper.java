package com.jhh.jhs.loan.mapper.manager;

import com.jhh.jhs.loan.entity.manager.Question;
import com.jhh.jhs.loan.entity.manager_vo.QuestionVo;

import java.util.List;

public interface QuestionMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Question record);

    int insertSelective(Question record);

    Question selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Question record);

    int updateByPrimaryKeyWithBLOBs(Question record);

    int updateByPrimaryKey(Question record);
    
    List<Question> selectAllQuestion();
    
    List<QuestionVo> getAllQuestionList();
}