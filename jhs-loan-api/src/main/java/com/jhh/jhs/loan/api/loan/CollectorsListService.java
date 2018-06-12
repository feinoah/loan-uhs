package com.jhh.jhs.loan.api.loan;


import com.jhh.jhs.loan.entity.loan.Collectors;

import java.util.List;

public interface CollectorsListService {

  /**
   * 批量分单
   * @param borrIds
   * @param collectors
   * @return
   */
    int batchUpdate(List borrIds, Collectors collectors);

  /**
   * 更新完成状态
   * @param borrIds
   * @return
   */
    int saveCompletionStatus(List borrIds);

  /**
   * 更新完成状态
   * @param borrId
   * @return
   */
  int saveCompletionStatus(int borrId);

  /**
   * 更新分期催收状态
   * @param borrId borrow_list
   * @param repaymentIds
   * @return
   */
  int updateStagesCollectionStatus(int borrId,int[] repaymentIds, String borrowStatus);
}
