package com.jhh.jhs.loan.mapper.loan;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Copyright © 2018 上海金互行金融服务有限公司. All rights reserved. *
 *
 * @Title:
 * @Prject: jhs-loan
 * @Package: com.jhh.jhs.loan.mapper.loan
 * @Description: 资金相关
 * @author: jack liujialin@jinhuhang.com.cn
 * @date: 2018/1/18 20:21
 * @version: V1.0
 */

@Repository
public interface TradeMapper {

    void updateLoadOrderAtActivityQuery(@Param("sid") String sid);

}
