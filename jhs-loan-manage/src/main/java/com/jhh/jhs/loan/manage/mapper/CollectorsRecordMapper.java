package com.jhh.jhs.loan.manage.mapper;

import com.jhh.jhs.loan.entity.manager.CollectorsRecord;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CollectorsRecordMapper extends Mapper<CollectorsRecord> {

    /**
     * 批量插入转件记录
     * @param collectorsRecords
     * @return
     */
    int batchInsertCollectorsRecord(@Param("list") List<CollectorsRecord> collectorsRecords);
}
