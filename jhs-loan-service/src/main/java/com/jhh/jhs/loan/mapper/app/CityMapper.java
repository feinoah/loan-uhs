package com.jhh.jhs.loan.mapper.app;

import java.util.List;

import com.jhh.jhs.loan.entity.app.City;

public interface CityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(City record);

    int insertSelective(City record);

    City selectByPrimaryKey(Integer id);
    
    List<City> findByPid(Integer pid);

    int updateByPrimaryKeySelective(City record);
        
    int updateByPrimaryKey(City record);
}