package com.jhh.jhs.loan.manage.mapper;

import com.jhh.jhs.loan.manage.entity.RobotQuestion;
import tk.mybatis.mapper.common.Mapper;

public interface RobotQuestionMapper extends Mapper<RobotQuestion> {

    int insertRobotQuestion(RobotQuestion robotQuestion);

}
