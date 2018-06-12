package com.jhh.jhs.loan.manage.mapper;

import com.jhh.jhs.loan.entity.app.BorrowList;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BorrowListMapper extends Mapper<BorrowList> {

    /**
     * 根据用户ID查询合同
     * @param perId
     * @return
     */
    List getBorrByPerId(Integer perId);

    /**
     * 查询逾期为分单合同
     * @return
     */
    List getCollectorsByOverdue();

    /**
     * 查询结清状态ID
     * @param borrIds
     * @return
     */
    List selectIdsBySettleStatus(List borrIds);

    /**
     * 人工审核拒绝
     * @return
     */
    int rejectAudit();

    /**
     * 查询该用户是否已经放过款
     * @param perId
     * @return
     */
    List<BorrowList> queryBorrListByPerIdAndStauts(Integer perId);

    /**
     * 查询昨天没打审核电话的首单用户
     * @return
     */
    List<BorrowList> selectUnBaikelu();

    /**
     * 根据合同id更改合同状态
     * @param borrId
     * @param status
     */
    void updateStatusById(@Param("borrId") Integer borrId, @Param("status") String status);

    /**
     * 根据合同id查询合同逾期还款金额
     * @param borrId
     */
    String getMstRepayAmount(Integer borrId);
}
