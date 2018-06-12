package com.jhh.jhs.loan.mapper.manager;


import com.jhh.jhs.loan.entity.loan.CollectorsList;
import tk.mybatis.mapper.common.Mapper;

public interface CollectorsListMapper extends Mapper<CollectorsList> {


    int updateCollectorsList(Integer borrId);

    String selectCollectUserByBorrId(Integer borrId);
    //把collections_list表的字段更新为1
    void deleteCollection(int borrId);
}
