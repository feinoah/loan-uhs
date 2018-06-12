package com.jhh.jhs.loan.mapper.phone;

import com.jhh.jhs.loan.entity.app.PhoneInfo;
import tk.mybatis.mapper.common.Mapper;

/**
 * 2018/1/16.
 */
public interface PhoneInfoMapper extends Mapper<PhoneInfo>{

    public PhoneInfo selectNow(String userId);
}
