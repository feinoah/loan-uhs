package com.jhh.jhs.loan.dao;

import com.jhh.jhs.loan.entity.loan.CollectorsList;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;


public interface CollectorsRecordMapper extends Mapper<CollectorsList> {


    /**
     * 查询催收人今天新增期数
     * @param map
     * @return
     */
    int selectAddPeriodsNumToday(Map map);
    /**
     * 查询催收人昨天新增期数
     * @param map
     * @return
     */
    int selectAddPeriodsNumYesterday(Map map);

}
