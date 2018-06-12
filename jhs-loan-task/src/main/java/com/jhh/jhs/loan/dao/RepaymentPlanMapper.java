package com.jhh.jhs.loan.dao;

import com.jhh.jhs.loan.entity.loan.CollectorsList;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;


public interface RepaymentPlanMapper extends Mapper<CollectorsList> {


    /**
     * 查询催收人员当天完成的催收单数
     * @return
     */
    int selectCompletePeriodsNum(Map map);



}
