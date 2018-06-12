package com.jhh.jhs.loan.api.loan;

import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.loan_vo.BatchCollectEntity;
import com.jhh.jhs.loan.entity.utils.Callback;

import java.util.List;


public interface YsbCollectionService {

    /**
     * 发起代扣请求
     * @param borrNum
     * @param name
     * @param idCardNo
     * @param optAmount
     * @param bankId
     * @param bankNum
     * @param phone
     * @param description
     * @return
     */
    public NoteResult askCollection(String guid, String borrNum, String name, String idCardNo,
                                    String optAmount, String bankId, String bankNum,
                                    String phone, String description, String serNo, String createUser, String collectionUser);

    /**
     * 处理第三方回调
     * @param callback
     * @return
     */
    public NoteResult collectCallback(Callback callback);
    
    /**
     * 定时任务，查询处理中的订单
     */
    public void queryCollectStatus();
    
    /**
     * .net单独查询订单状态接口
     */
    public NoteResult netQueryOrder(String serNo);

    /**批量代扣接口
     * @param param
     * @return
     */
    public NoteResult askCollectionBatch(List<BatchCollectEntity> param);

    public String testCallback(String orderId);

    /**单笔订单查询接口  
     * @param serialNo
     * @return
     */
    public NoteResult orderStatus(String serialNo);


   }
