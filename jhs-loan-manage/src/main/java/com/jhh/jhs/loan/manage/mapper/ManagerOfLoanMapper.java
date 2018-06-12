package com.jhh.jhs.loan.manage.mapper;

import com.jhh.jhs.loan.entity.manager_vo.CardPicInfoVo;
import com.jhh.jhs.loan.entity.manager_vo.LoanInfoVo;
import com.jhh.jhs.loan.entity.manager_vo.PrivateVo;

import java.util.List;

public interface ManagerOfLoanMapper {

    PrivateVo selectUserPrivateVo(int perid);

    CardPicInfoVo getCardPicById(int himid);

    List<LoanInfoVo> selectLoanInfoPrivateVo(int himid);

    List<LoanInfoVo> selectLoanInfoPrivateVoForOperator(int himid);
}
