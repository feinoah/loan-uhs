package com.jhh.jhs.loan.mapper.manager;

import com.jhh.jhs.loan.entity.manager.Review;
import com.jhh.jhs.loan.entity.manager_vo.ReviewVo;

import java.util.List;

public interface ReviewMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Review record);

    int insertSelective(Review record);

    Review selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Review record);

    int updateByPrimaryKey(Review record);
     
    int blackListStatus(String brro_id);
    //获取审核表总数
    Integer reviewSum();
    List<ReviewVo> getReviewVoBlackList(int himid);

    //查询合同是否有审核人
    int selectReview(Integer borrId);

    Review selectByBorrId(Integer borrId);
    
}