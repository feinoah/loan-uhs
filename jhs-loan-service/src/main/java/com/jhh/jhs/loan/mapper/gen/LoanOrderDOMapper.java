package com.jhh.jhs.loan.mapper.gen;

import com.jhh.jhs.loan.mapper.gen.domain.LoanOrderDO;
import com.jhh.jhs.loan.mapper.gen.domain.LoanOrderDOExample;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface LoanOrderDOMapper {
    int countByExample(LoanOrderDOExample example);

    int deleteByExample(LoanOrderDOExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LoanOrderDO record);

    int insertSelective(LoanOrderDO record);

    List<LoanOrderDO> selectByExample(LoanOrderDOExample example);

    LoanOrderDO selectByPrimaryKey(Integer id);

    LoanOrderDO selectSubOrderByPid(Integer pid);

    Double getListByBorrId(Integer borrId);

    LoanOrderDO selectBySerNo(@Param("serNo") String serNo);

    int updateByExampleSelective(@Param("record") LoanOrderDO record, @Param("example") LoanOrderDOExample example);

    int updateByExample(@Param("record") LoanOrderDO record, @Param("example") LoanOrderDOExample example);

    int updateByPrimaryKeySelective(LoanOrderDO record);

    int updateByPrimaryKey(LoanOrderDO record);

    int updateStatusById(@Param("orderId")Integer orderId,@Param("status") String status,@Param("rlRemark") String msg);

    LoanOrderDO selectByGuid(String guid);

    int updateChannelsBySerialNo(@Param("map") Map<String,Object> map);
}