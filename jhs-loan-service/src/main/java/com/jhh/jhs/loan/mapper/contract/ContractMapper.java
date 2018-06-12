package com.jhh.jhs.loan.mapper.contract;

import com.jhh.jhs.loan.entity.contract.Contract;
import tk.mybatis.mapper.common.Mapper;

public interface ContractMapper extends Mapper<Contract> {
    int insertContract(Contract contract);

    String getContractUrl(String borrId);
}