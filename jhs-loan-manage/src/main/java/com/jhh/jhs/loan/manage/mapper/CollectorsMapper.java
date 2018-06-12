package com.jhh.jhs.loan.manage.mapper;

import com.jhh.jhs.loan.entity.loan.Collectors;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface CollectorsMapper extends Mapper<Collectors> {

    Integer selectMaxId();

    List<Collectors> selectDsUsers(Map<String, Object> params);

    String getChannelSource(String username);

    String getSysNoByName(String userName);

    String getNameBySysNo(String SysNo);

    List<Collectors> selectUserBySysNo(String sysNo);

    List<Collectors> queryChannelCollectors();
}
