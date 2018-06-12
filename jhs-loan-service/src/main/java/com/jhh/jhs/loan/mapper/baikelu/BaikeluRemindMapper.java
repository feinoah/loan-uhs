package com.jhh.jhs.loan.mapper.baikelu;

import com.jhh.jhs.loan.entity.baikelu.BaikeluRemind;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaikeluRemindMapper extends Mapper<BaikeluRemind> {
    public void insertBaikeluRemindList(List<BaikeluRemind> list);
    public void updateBaikeluRemindList(List<BaikeluRemind> list);
}