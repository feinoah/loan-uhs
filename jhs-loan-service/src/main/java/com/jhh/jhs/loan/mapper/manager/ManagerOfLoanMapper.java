package com.jhh.jhs.loan.mapper.manager;

import com.jhh.jhs.loan.entity.app.Image;
import com.jhh.jhs.loan.entity.app.Reviewers;
import com.jhh.jhs.loan.entity.manager.Review;
import com.jhh.jhs.loan.entity.manager_vo.*;

import java.util.List;

public interface ManagerOfLoanMapper {
    

    int updateByPrimaryKeySelective(ReqBackPhoneCheckVo record);
    
    List<Reviewers> selectRiewerList(String status);
    List<Reviewers> selectRiewerListAll();
    
    PrivateVo selectUserPrivateVo(int perid);
    
    List<LoanInfoVo>  selectLoanInfoPrivateVo(int himid);
    
    List<BankInfoVo> selectBankInfoVo(int himid);
    
    int personCheckMessage(Review record);
    int transferPersonCheck(Review record);
    
    CardPicInfoVo getCardPicById(int himid);
    List<Image> PicBatchVo(int pageIndex, int pageSize);
}