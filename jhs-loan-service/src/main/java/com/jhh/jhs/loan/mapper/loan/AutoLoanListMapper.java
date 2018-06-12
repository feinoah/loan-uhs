package com.jhh.jhs.loan.mapper.loan;

import com.jhh.jhs.loan.entity.loan.AutoLoanList;

/**
 *  自动放款配置流水表
 */
public interface AutoLoanListMapper {

    /**
     *  查询表中最新一条数据规则
     * @return
     */
    public AutoLoanList selectByMax();
	/**
	 *  保存最新放款条件
     *
     */
    public void saveAutoLoanList(AutoLoanList list);

}
