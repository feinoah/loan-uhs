package com.jhh.jhs.loan.manage.mapper;

import com.jhh.jhs.loan.entity.loan.CollectorsList;
import com.jhh.jhs.loan.entity.loan.ExportWorkReport;
import com.jhh.jhs.loan.entity.manager.CollectorsCompanyVo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;


public interface CollectorsListMapper extends Mapper<CollectorsList> {

    /**
     * 根据合同ID获取催收人及所属公司
     *
     * @param map
     * @return
     */
    CollectorsCompanyVo selectCollectorsCompanyVo(Map<String, Object> map);

    int batchInsertCollectorsList(@Param("list") List<CollectorsList> list);

    /**
     * 获取工作报告
     *
     * @param map
     * @return
     */
    List<ExportWorkReport> getWorkReport(Map<String, Object> map);

    /**
     * 批量更新
     *
     * @param list
     */
    int batchUpdateCollectorsList(@Param("list") List<CollectorsList> list);

}
