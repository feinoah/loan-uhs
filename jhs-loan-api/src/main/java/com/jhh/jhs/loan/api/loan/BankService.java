package com.jhh.jhs.loan.api.loan;

import com.jhh.jhs.loan.api.entity.BindCardVo;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.AgentDeductRequest;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.pay.driver.pojo.BankBaseInfo;

/**
 * 绑定银行卡
 * @author xuepengfei
 *2016年11月4日上午9:35:34
 */
public interface BankService {

    
    /**绑定银行卡
     * @param per_id
     * @param bank_num
     * @param phone 
     * @param status
     * @param tokenKey
     * @return
     */
    public NoteResult bindingBank(String per_id, String bankCode, String bank_num, String phone, String status, String tokenKey,String device);

    /**查询绑定银行卡子协议
     * @param per_id
     * @param bank_num
     * @return
     */
    public NoteResult queryContractId(String per_id, String bank_num);

    /**认证白骑士
     * @param per_id
     * @param name
     * @param card_num
     * @param bank_num
     * @param bank_phone
     * @param tokenKey
     * @return
     */
    public boolean verifyBQS(String per_id, String name, String card_num, String bank_num, String bank_phone, String tokenKey,String device);

    /**绑卡白骑士
     * @param per_id
     * @param name
     * @param card_num
     * @param bank_num
     * @param bank_phone
     * @param tokenKey
     * @return
     */
    public boolean bindingBQS(String per_id, String name, String card_num, String bank_num, String bank_phone, String tokenKey,String device);
    
    /**
     * 用户所有银行卡
     * 
     * @param per_id
     * @return
     */
    public NoteResult personBanks(String per_id);

    /**
     * 用户副卡转主卡
     * 
     * @param per_id
     * @param bank_num
     * @return
     */
    public boolean changeBankStatus(String per_id, String bank_num);

    /**
     * 获取可以代扣及支付的银行卡列表
     * @return
     */
    public NoteResult getBankList();

    NoteResult payCenterBindCard(BindCardVo vo);

}
