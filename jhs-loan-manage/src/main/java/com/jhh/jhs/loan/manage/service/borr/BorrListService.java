package com.jhh.jhs.loan.manage.service.borr;


import com.jhh.jhs.loan.manage.entity.Response;

import java.util.List;

public interface BorrListService {

  /**
   * 获取合同
   * @param perId
   * @return
   */
  Response getBorrByPerId(Integer perId);


  /**
   * 取消合同
   * @param id
   * @return
   */
  Response cancelBorrList(Integer id);

  /**
   * 合同转件
   * @param borrIds 合同号
   * @param userId 转至人ID
   * @param opUserId 操作人ID
   * @return
   */
  Response saveTransferBorrList(List<String> borrIds, String userId,String opUserId);

  /**
   * 自动分单
   * 分单特殊
   */
  Integer submenuTransfer();

  /**
   * 人工审核跑批
   */
  void rejectAudit();
  /**
   * 百可录打电话
   */
  void rcCallPhone();
}
