package com.jhh.jhs.loan.api.app;

import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.agreement.BorrowAgreement;
import com.jhh.jhs.loan.entity.app.NoteResult;

import java.util.Map;

/**
 *  借款模块接口
 * @author xuepengfei
 *2016年9月28日上午9:31:58
 */
public interface LoanService {



    /**
     * 用户借款状态节点
     * @param per_id 用户ID
     * @return
     */
    public NoteResult getBorrStatus(String per_id);

    /**
     * 生成借款记录
     * @param per_id 用户ID
     * @param product_id 产品ID
     * @return
     */
    public ResponseDo<?> borrowProduct(String userId,String productId,String borrAmount);



    /**
     * 插入身份证正面信息
     * @param per_id 用户ID
     * @param card_json 身份证信息json串
     * @param card_byte 身份证正面照片流
     * @return
     */
    public NoteResult insertCardInfoz(String per_id, String card_num, String name, String sex,
                                      String nation, String birthday, String address,
                                      String card_byte, String head_byte, String description);

    /**
     * 插入身份证反面信息
     * @param per_id 用户ID
     * @param card_json 身份证信息json串
     * @param card_byte 身份证反面照片流
     * @return
     */
    public NoteResult insertCardInfof(String per_id, String office, String start_date,
                                      String end_date, String card_byte, String description);


    /**
     * 插入个人信息
     * @param per_id 用户ID @param qq_num QQ号  @param email 邮箱
     * @param usuallyaddress 常用地址 @param education 学历
     * @param marry 婚姻状况  @param getchild 生育状况
     * @param profession 职业 @param monthlypay 月薪
     * @param business 单位名 @param busi_province 单位所在省
     * @param busi_city 单位所在市 @param busi_address 单位详细地址
     * @param busi_phone 单位电话 @param relatives 亲属关系
     * @param relatives_name 亲属名字 @param rela_phone 亲属联系方式
     * @param society 社会关系 @param soci_phone 社会联系方式
     * @param society_name 社会关系名字
     * @return
     */
    public NoteResult insertPrivateInfo(String per_id, String qq_num, String email,
                                        String usuallyaddress, String education, String marry,
                                        String getchild, String profession, String monthlypay,
                                        String business, String busi_province, String busi_city,
                                        String busi_address, String busi_phone, String relatives,
                                        String relatives_name, String rela_phone, String society,
                                        String soci_phone, String society_name, String tokenKey, String device);

    /**
     * 获取自己的手机号，及运营商展示
     * @param per_id 用户ID
     * @return
     */
    public NoteResult getPhoneInfo(String per_id);

    /**
     * 获取用户本地的手机通讯录名单
     * @param per_id 用户ID
     * @param phone_list 用户本地的手机通讯录名单
     * @return
     */
    public NoteResult getPhoneList(String per_id, String phone_list);

     /**
     * 认证完成，修改用户、流程、借款状态
     * @param per_id 用户ID
     * @return
     */
    public NoteResult bpmFinish(String per_id);


    /**
     * 合同签约，状态改为已签约，添加签约时间
     * @param per_id 用户ID
     * @param borr_id 合同id
     * @param prod_id 产品id
     * @return
     */
    public NoteResult signingBorrow(String per_id, String borr_id);

    /**
     * 取消借款申请。判断合同状态，在申请中的合同才能取消借款申请。
     * @param per_id 用户ID
     * @param borr_id 合同id
     * @return
     */
    public NoteResult cancelAskBorrow(String per_id, String borr_id);

    /**
     * 根据用户id查询姓名及身份证号
     * @param per_id  用户id
     * @return
     */
    public NoteResult getIDNumber(String per_id);

    /**
     * 查询所有省市信息
     * @return
     */
    public NoteResult getCity();


    /**
     * 人脸识别页面   获取身份证正面照
     * @param per_id
     * @return
     */
    public NoteResult getCardz(String per_id);

    /**
     * 获取姓名及手机号
     * @param per_id
     * @return
     */
    public NoteResult getNamePhone(String per_id);

    /**
     * 获取首页滚动条显示内容
     * @param per_id
     * @return
     */
    public ResponseDo<Map<String,Object>> getRolling(String per_id, String device);

    /**验证tokenId
     * @param per_id
     * @param tokenId
     * @return
     */
    public String verifyTokenId(String per_id, String token);

//    /**身份证正面认证（骏聿）
//     * @param per_id
//     * @param photo
//     * @return
//     */
//    public NoteResult ocrFront(String per_id,String photo);
//
//    /**身份证反面认证（骏聿）
//     * @param per_id
//     * @param photo
//     * @return
//     */
//    public NoteResult ocrBack(String per_id,String photo);
//
//    /**人脸识别认证（骏聿）
//     * @param per_id
//     * @param photos
//     * @return
//     */
//    public NoteResult compareAll(String per_id,String photos);

    /**还款页面信息
     * @param per_id
     * @return
     */
    public NoteResult repayInfo(String per_id);

    // public NoteResult cardOcr(String headFile, String cardFile);

    /**
     * 用户是否可以绑银行卡（未经过个人认证的用户不可以绑卡）
     * @param per_id
     * @return
     */
    public NoteResult canBinding(String per_id);

    /**
     * 存储人脸识别的带环境照片
     * @param bytes
     * @param per_id
     * @return
     */
    public NoteResult saveVerifyPhoto(byte[] bytes, String per_id);

    /**
     * 获取聚信立协议
     * @return
     */
    public NoteResult juxinliInfo();

    /**
     * 人工审核通用接口 （给用户标记状态、解除状态）
     * @param perId
     * @param type
     * @param isManual
     * @param description
     * @return
     */
    public boolean manuallyReview(String perId, int type, int isManual, String description);

    /**
     * 获取签约界面信息
     * @param per_id
     * @return
     */
    public NoteResult getSignInfo(String per_id,String device,String token);

    /**
     *  获取借款协议信息
     * @return
     */
    ResponseDo<BorrowAgreement> getBorrowAgreement(String perId);

    /**
     * 根据合同编号查询催收人
     * @param borrId
     */
    String getCollectionUser(int borrId);

    /**
     * 身份证orc 走风控
     * @param cardFile
     * @param headFile
     * @param perId
     * @return
     */
    JSONObject cardOcrAndRisk(byte[] cardFile, byte[] headFile, String perId);
}
