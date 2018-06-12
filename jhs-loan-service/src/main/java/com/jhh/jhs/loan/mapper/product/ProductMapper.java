package com.jhh.jhs.loan.mapper.product;

import com.jhh.jhs.loan.entity.app.Product;
import com.jhh.jhs.loan.entity.app_vo.SignInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 2017/12/28.
 */
public interface ProductMapper extends Mapper<Product> {

   public List<Product> selectByDevice(String device);

   public List<Product> getProductAll();

   public SignInfo getSignInfo(String per_id);
}
