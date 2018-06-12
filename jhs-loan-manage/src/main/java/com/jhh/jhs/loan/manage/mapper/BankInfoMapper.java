package com.jhh.jhs.loan.manage.mapper;

import com.jhh.jhs.loan.entity.app.Bank;
import com.jhh.jhs.loan.entity.app.BankVo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface BankInfoMapper extends Mapper<Bank> {

    BankVo selectBankInfos(Map<String, Object> map);


    List<BankVo> selectBankInfosByPerId(Map<String, Object> map);

    /**
     * 查询用户主卡
     * @param userId
     * @return
     */
    BankVo selectMainBankByUserId(Integer userId);

    Bank selectByBankNumAndStatus(String bankNum);
}
