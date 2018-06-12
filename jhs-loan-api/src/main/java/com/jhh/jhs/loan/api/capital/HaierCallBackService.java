package com.jhh.jhs.loan.api.capital;


import com.jhh.jhs.loan.entity.app.NoteResult;

/**
 * 海尔支付回调接口
 */
public interface HaierCallBackService {


    /***
     *   代扣交易查询网关接口
     * @return
     * @throws Exception
     */
    public NoteResult queryTrade() ;

    /**
        主动还款定时查询
     */
    public NoteResult queryAppRpay();

    /**
     * 根据订单号单笔查询代扣接口
     * @param orderId
     * @return
     */
    public NoteResult orderStatus(String orderId);

    /**
     * 根据订单号单笔查询代付接口
     * @param orderId
     * @return
     */
    public NoteResult payOrderStatus(String orderId);

}
