package com.jhh.jhs.loan.mapper.product;

import com.jhh.jhs.loan.entity.app.ProductTerm;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

/**
 * 2018/1/5.
 */
public interface ProductTermMapper extends Mapper<ProductTerm>{

    /**
     * 根据产品ID查询期数表
     * @param term_id
     * @return
     */
    ProductTerm selectById(@Param("term_id") Integer term_id);
}
