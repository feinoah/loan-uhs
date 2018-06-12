package com.jhh.jhs.loan.manage.mapper;

import com.jhh.jhs.loan.entity.manager_vo.CommissionDetailVo;
import com.jhh.jhs.loan.entity.share.CommissionOrder;
import com.jhh.jhs.loan.entity.share_vo.CommissionOrderVO;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.formula.functions.T;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CommissionOrderMapper extends Mapper<CommissionOrder> {

    Map<String, BigDecimal> queryTotalAmount(String perId);

    int deleteByPrimaryKey(Integer id);

    int insert(CommissionOrder record);

    int insertSelective(CommissionOrder record);

    CommissionOrder selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CommissionOrder record);

    int updateByPrimaryKey(CommissionOrder record);

    int updateWithDrawalStatus(@Param("status") String status, @Param("list") List list);

    /**
     * 查询邀请人
     * @param per_id person id
     * @param level 1,一级邀请，2,二级邀请
     * @param start
     * @param pageSize
     */
    List<CommissionOrderVO> selectInviter(@Param("perId") Integer per_id, @Param("level") String level, @Param("start") int start, @Param("pageSize") int pageSize,@Param("phone") String phone);

    /**
     * 计算数量
     * @param per_id
     * @param level
     * @param start
     * @param pageSize
     * @param phone
     * @return
     */
    int selectInviterCount(@Param("perId") Integer per_id, @Param("level") String level, @Param("start") int start, @Param("pageSize") int pageSize,@Param("phone") String phone);

    List<CommissionDetailVo> queryLevel1CommissionOrderByPersonId(@Param("personId") String personId,@Param("phone") String phone);
}
