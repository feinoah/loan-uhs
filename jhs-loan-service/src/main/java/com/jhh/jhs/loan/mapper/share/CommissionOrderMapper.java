package com.jhh.jhs.loan.mapper.share;

import com.jhh.jhs.loan.entity.manager_vo.CommissionDetailVo;
import com.jhh.jhs.loan.entity.share.CommissionOrder;
import com.jhh.jhs.loan.entity.share_vo.CommissionOrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommissionOrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CommissionOrder record);

    int insertSelective(CommissionOrder record);

    CommissionOrder selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CommissionOrder record);

    int updateByPrimaryKey(CommissionOrder record);

    List<CommissionOrder> selectByConditions(CommissionOrder commissionOrder);

    List<CommissionDetailVo> queryLevelCommissionOrderByPersonId(@Param("personId") String personId, @Param("level") String level, @Param("start") int start, @Param("pageSize") int pageSize);

    int updateCommissionOrderStatus(@Param("status") String status, @Param("ids") String[] ids);

}