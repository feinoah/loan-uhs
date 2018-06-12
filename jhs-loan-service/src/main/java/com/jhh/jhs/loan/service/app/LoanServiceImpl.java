package com.jhh.jhs.loan.service.app;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.app.LoanService;
import com.jhh.jhs.loan.api.app.RiskService;
import com.jhh.jhs.loan.api.app.UserService;
import com.jhh.jhs.loan.api.commission.CommissionRuleService;
import com.jhh.jhs.loan.api.constant.Constants;
import com.jhh.jhs.loan.api.constant.StateCode;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.agreement.BorrowAgreement;
import com.jhh.jhs.loan.common.util.*;
import com.jhh.jhs.loan.constant.NodeConstant;
import com.jhh.jhs.loan.entity.app.*;
import com.jhh.jhs.loan.entity.app_vo.SignInfo;
import com.jhh.jhs.loan.entity.manager.Review;
import com.jhh.jhs.loan.mapper.app.*;
import com.jhh.jhs.loan.mapper.manager.MsgMapper;
import com.jhh.jhs.loan.mapper.manager.ReviewMapper;
import com.jhh.jhs.loan.mapper.phone.PhoneInfoMapper;
import com.jhh.jhs.loan.mapper.product.BannerMapper;
import com.jhh.jhs.loan.mapper.product.ProductMapper;
import com.jinhuhang.risk.dto.FeatureDto;
import com.jinhuhang.risk.dto.PersonAuthDto;
import com.jinhuhang.risk.service.contacts.ContactsAPI;
import com.jinhuhang.risk.service.impl.contacts.ContactsAPIClient;
import com.jinhuhang.risk.service.impl.orc.OCRAPIAPIClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import redis.clients.jedis.JedisCluster;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import tk.mybatis.mapper.entity.Example;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 借款模块接口实现类
 *
 * @author xuepengfei
 *         2016年10月9日下午3:40:59
 */
@Service
public class LoanServiceImpl implements LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanServiceImpl.class);

    //返回参数及消息设置
    private static final String SUCCESS_CODE = CodeReturn.SUCCESS_CODE;
    private static final String SUCCESS_INFO = "成功";
    private static final String FAIL_CODE = CodeReturn.FAIL_CODE;
    private static final String FAIL_INFO = "失败";
    private static final String NOW_BORROW_CODE = CodeReturn.NOW_BORROW_CODE;
    private static final String NOW_BORROW_INFO = "现在借款";
    private static final String WAIT_SIGN_CODE = CodeReturn.WAIT_SIGN_CODE;
    private static final String WAIT_SIGN_INFO = "现在签约";
    private static final String SIGNED_CODE = CodeReturn.SIGNED_CODE;
    private static final String SIGNED_INFO = "等待放款";
    private static final String TOREPAY_CODE = CodeReturn.TOREPAY_CODE;
    private static final String TOREPAY_INFO = "现在还款";
    private static final String APPLY_CODE = CodeReturn.APPLY_CODE;
    private static final String APPLY_INFO = "继续申请";
    //认证状态常量
    private static final String BPM_UNDO_CODE = CodeReturn.BPM_UNDO_CODE;
    private static final String BPM_ZHIMA_CODE = CodeReturn.BPM_ZHIMA_CODE;
    //未认证
    private static final String BPM_UNDO_INFO = "未认证";
    //已认证
    private static final String BPM_FINISH_CODE = CodeReturn.BPM_FINISH_CODE;
    private static final String BPM_FINISH_INFO = "已认证";
    // 已认证 但重新拉通讯录
    private static final String BPM_PHONE_CODE = CodeReturn.BPM_PHONE_CODE;
    private static final String BPM_PHONE_INFO = "重新获取通讯录";// 已认证 但重新拉通讯录

    //认证状态编码
    private static final String STATUS_BPM_N = CodeReturn.STATUS_BPM_N;//未认证
    private static final String STATUS_BPM_Y = CodeReturn.STATUS_BPM_Y;//已认证
    private static final String STATUS_BPM_FAIL = CodeReturn.STATUS_BPM_FAIL;//认证失败
    private static final String STATUS_BPM_FAIL_B = CodeReturn.STATUS_BPM_FAIL_B;//认证失败且进黑名单
    private static final String STATUS_BPM_UP = CodeReturn.STATUS_BPM_UP;//已提交,仅手机认证节点有次状态
    //认证过期天数
    private static final int LIMIT_DAY = CodeReturn.LIMIT_DAY;
    //拒绝以后不能再次申请的天数
    private static final int REFUSE_DAY = CodeReturn.REFUSE_DAY;
    //借款状态常量
    private static final String STATUS_APLLY = CodeReturn.STATUS_APLLY;//申请中
    private static final String STATUS_CANCEL = CodeReturn.STATUS_CANCEL;//已取消
    private static final String STATUS_WAIT_SIGN = CodeReturn.STATUS_WAIT_SIGN;//待签约
    private static final String STATUS_SIGNED = CodeReturn.STATUS_SIGNED;//已签约
    private static final String STATUS_TO_REPAY = CodeReturn.STATUS_TO_REPAY;//待还款
    private static final String STATUS_LATE_REPAY = CodeReturn.STATUS_LATE_REPAY;//逾期未还
    private static final String STATUS_PAY_BACK = CodeReturn.STATUS_PAY_BACK;//已还清
    private static final String STATUS_REVIEW_FAIL = CodeReturn.STATUS_REVIEW_FAIL;//审核未通过
    private static final String STATUS_PHONE_REVIEW_FAIL = CodeReturn.STATUS_PHONE_REVIEW_FAIL;//电审未通过
    private static final String STATUS_DELAY_PAYBACK = CodeReturn.STATUS_DELAY_PAYBACK;//逾期还清
    private static final String STATUS_COM_PAYING = CodeReturn.STATUS_COM_PAYING;//放款中
    private static final String STATUS_COM_PAY_FAIL = CodeReturn.STATUS_COM_PAY_FAIL;//放款失败
    private static final String STATUS_PAYING = CodeReturn.STATUS_PAYING;// 还款中

    private static final String dfsUrl = PropertiesReaderUtil.read("third", "dfsUrl");

    //Guid没有匹配时，返回的code
    private static final String GUID_WRONG = CodeReturn.GUID_WRONG;//返回系统繁忙,暂时用聚信立的系统繁忙状态码

    private static final String RISK_SUCCESS_CODE = CodeReturn.RISK_SUCCESS_CODE;


    static BASE64Decoder decoder = new sun.misc.BASE64Decoder();

    @Autowired
    private BpmNodeMapper bpmNodeMapper;
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private BorrowListMapper borrowListMapper;
    @Autowired
    private PrivateMapper privateMapper;
    @Autowired
    private CityMapper cityMapper;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private ReviewersMapper reviewersMapper;
    @Autowired
    private MsgMapper msgMapper;
    @Autowired
    private JedisCluster jedisCluster;
    @Autowired
    private CodeValueMapper codeValueMapper;
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private BannerMapper bannerMapper;
    @Autowired
    private RiskService riskService;
    @Autowired
    private UserService userService;
    @Autowired
    private AgreementServiceImpl agreementService;

    @Autowired
    private PhoneInfoMapper phoneInfoMapper;
    @Autowired
    CommissionRuleService commissionRuleService;

    @Value("${productId}")
    private String productId;
    @Value("${yhs_servicecall}")
    private String serviceCall;
    @Value("${yhs_service_work_start}")
    private String workStart;
    @Value("${yhs_service_work_end}")
    private String workEnd;

    @Override
    public ResponseDo<BorrowAgreement> getBorrowAgreement(String perId) {
        return agreementService.getBorrowAgreement(perId);
    }

    @Override
    public String getCollectionUser(int borrId) {
        BorrowList borrow = borrowListMapper.getBorrowListByBorrId(borrId);
        if (borrow != null) {
            return borrow.getCollectionUser();
        }
        return null;
    }

    /**
     * 用户借款状态节点
     *
     * @param per_id 用户ID
     * @return
     */
    public NoteResult getBorrStatus(String per_id) {
        // 构建结果对象NoteResult 默认 code=202 info=现在借款 (包括没有认证流程，没有借款记录，认证流程过期，身份证过期)
        NoteResult result = new NoteResult(NOW_BORROW_CODE, NOW_BORROW_INFO);
        result.setData("");

        // 查询当前的借款表
        BorrowList borr = borrowListMapper.selectNow(Integer.valueOf(per_id));

        if (borr == null) {
            return result;
        }
        String borr_status = borr.getBorrStatus();
        // 判断当前的借款状态
        if (STATUS_PAY_BACK.equals(borr_status) || STATUS_CANCEL.equals(borr_status)
                || STATUS_DELAY_PAYBACK.equals(borr_status) || STATUS_PHONE_REVIEW_FAIL.equals(borr_status)) {
            // 已还清或已取消或逾期还清，返回现在借款
            // 检查是否是黑名单用户
            Person p = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
            if (p != null && p.getCardNum() != null && !riskService.isBlack(p.getPhone(), p.getCardNum())) {
                result.setCode("206");
                //是黑名单
                result.setInfo("黑名单");
                result.setData("r");
            }
            return result;
        } else if (STATUS_APLLY.equals(borr_status) || STATUS_REVIEW_FAIL.equals(borr_status)) {
            // 申请中,或者审核被拒绝
            // 检查是否是黑名单用户
            boolean isManual = borr.getIsManual() != null && borr.getIsManual() != 4;
            //新增人工审核
            if (STATUS_APLLY.equals(borr_status) && isManual) {
                //申请中，并且有人工审核记录
                result.setCode(APPLY_CODE);
                result.setInfo("人工审核");
                result.setData(9);
                return result;
            }

            // 查询是否认证完成
            NoteResult check = riskService.checkBpm(per_id);
            // 认证未完成 --->申请中(206--返回当前未认证节点)
            if (BPM_UNDO_CODE.equals(check.getCode())) {
                if (STATUS_REVIEW_FAIL.equals(borr_status) && !"r".equals(check.getData().toString())) {
                    //新增审核拒绝的判断  如果审核拒绝超过了一定期限，可以重新认证了，把原借款改成取消 让用户重新借款
                    logger.info("O(∩_∩)OO(∩_∩)OO(∩_∩)OO(∩_∩)OO(∩_∩)OO(∩_∩)OO(∩_∩)O");
                    borr.setBorrStatus(STATUS_CANCEL);
                    int u = borrowListMapper.updateByPrimaryKey(borr);
                    if (u > 0) {
                        result.setCode(NOW_BORROW_CODE);
                        result.setInfo(NOW_BORROW_INFO);
                        return result;
                    }
                }
                // 返回当前未认证节点
                result.setCode(APPLY_CODE);
                result.setInfo(APPLY_INFO);
                result.setData(check.getData());// 未认证节点
                return result;
            } else if (BPM_FINISH_CODE.equals(check.getCode())) {

                if (STATUS_REVIEW_FAIL.equals(borr_status)) {
                    //如果认证都完成了 借款状态是审核拒绝 那要判断时间是否超过一个月  超过一个月重新申请
                    Date update = borr.getUpdateDate();
                    long now = System.currentTimeMillis();
                    if (now - update.getTime() < 30 * 24 * 60 * 60 * 1000L) {//不够30天
                        // 现在距离被拒绝时间小于30天，返回拒绝黄框
                        result.setCode(APPLY_CODE);
                        result.setInfo(APPLY_CODE);
                        result.setData("r");
                        return result;
                    } else {
                        // 不小于30天 ，取消当前借款 返回现在借款
                        logger.info("^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^");
                        borr.setBorrStatus(STATUS_CANCEL);
                        int u = borrowListMapper.updateByPrimaryKey(borr);
                        if (u > 0) {
                            result.setCode(NOW_BORROW_CODE);
                            result.setInfo(NOW_BORROW_INFO);
                            return result;
                        }
                    }
                }

                // 如果认证完成 把借款改为待签约 进签约页面  bpmFinish
                NoteResult finish = bpmFinish(per_id);
                if (SUCCESS_CODE.equals(finish.getCode())) {
                    //非人工审核
                    result.setCode(WAIT_SIGN_CODE);
                    result.setInfo(WAIT_SIGN_INFO);
                    result.setData(borr.getId());
                    return result;

                } else if (NOW_BORROW_CODE.equals(finish.getCode())) {
                    //人工审核
                    result.setCode(APPLY_CODE);
                    result.setInfo("人工审核");
                    result.setData(9);
                    return result;
                } else {
                    //系统错误
                    return new NoteResult(FAIL_CODE, FAIL_INFO);
                }
            } else if (BPM_PHONE_CODE.equals(check.getCode()) || BPM_ZHIMA_CODE.equals(check.getCode())) {
                //通讯录节点 或者芝麻信用节点 直接返回 check结果
                return check;
            }
        } else if (STATUS_WAIT_SIGN.equals(borr_status)) {
            // 待签约
            // 查询认证情况
            NoteResult check = riskService.checkBpm(per_id);
            // 有认证过期，将借款状态改为申请中，返回申请中
            if (BPM_UNDO_CODE.equals(check.getCode())) {
                borr.setBorrStatus(STATUS_APLLY);
                borrowListMapper.updateByPrimaryKeySelective(borr);
                // 返回当前未认证节点
                result.setCode(APPLY_CODE);
                result.setInfo(APPLY_INFO);
                result.setData(check.getData());// 未认证节点
                return result;
            } else if (BPM_PHONE_CODE.equals(check.getCode())) {
                return check;
            } else {// 认证没有问题
                result.setCode(WAIT_SIGN_CODE);
                result.setInfo(WAIT_SIGN_INFO);
                result.setData(borr.getId());
                return result;
            }
        } else if (STATUS_SIGNED.equals(borr_status) || STATUS_COM_PAYING.equals(borr_status)
                || STATUS_COM_PAY_FAIL.equals(borr_status)) {
            // 已签约
            result.setCode(SIGNED_CODE);
            result.setInfo(SIGNED_INFO);
            result.setData(borr.getId());
            return result;
        } else if (STATUS_TO_REPAY.equals(borr_status) || STATUS_LATE_REPAY.equals(borr_status)) {
            // 待还款、逾期未还
            result.setCode(TOREPAY_CODE);
            result.setInfo(TOREPAY_INFO);
            result.setData(borr.getId());
        } else if (STATUS_PAYING.equals(borr_status)) {
            // 2017.5.9更改 新加还款中状态
            result.setCode(CodeReturn.PAYING_CODE);
            result.setInfo("还款中");
        }
        return result;
    }

    /**
     * 生成借款记录
     *
     * @return
     */
    public ResponseDo<?> borrowProduct(String userId, String productId, String borrAmount) {
        logger.info("生成借款记录 userId =" + userId + "\nproductId=" + productId + "\nborrAmount=" + borrAmount);
        ResponseDo<?> result = new ResponseDo();
        // 幂等操作
        if (StringUtils.isEmpty(jedisCluster.get(RedisConst.BP_KEY + userId))) {
            String setnx = jedisCluster.set(RedisConst.BP_KEY + userId, userId, "NX", "EX", 60);
            if (!"OK".equals(setnx)) {
                result.setCode(StateCode.ORDER_REPEAT_CODE);
                result.setInfo(StateCode.ORDER_REPEAT_MSG);
                logger.error("borrowProduct直接返回，重复数据per_id" + userId);
                return result;
            }
        } else {
            result.setCode(StateCode.ORDER_REPEAT_CODE);
            result.setInfo(StateCode.ORDER_REPEAT_MSG);
            logger.error("borrowProduct直接返回，重复数据per_id" + userId);
            return result;
        }
        //新建borr时先检查是否有申请中的借款
        Integer haveBorrowing = borrowListMapper.selectDoing(Integer.parseInt(userId));
        if (haveBorrowing > 0) {//有电审未通过，已结清，已取消，已逾期结清之外的借款
            logger.error("有电审未通过，已结清，已取消，已逾期结清之外的借款");
            result.setCode(StateCode.ORDER_UNFINISHED_CODE);
            result.setInfo(StateCode.ORDER_UNFINISHED_MSG);
            return result;
        }
        //根据产品id 查询产品
        Product product = productMapper.selectByPrimaryKey(Integer.parseInt(productId));
        if (product == null) {
            logger.error("产品为空");
            result.setCode(StateCode.PRODUCT_EMPTY_CODE);
            result.setInfo(StateCode.PRODUCT_EMPTY_MSG);
            return result;
        }
        BorrowList newBlist = borrowListMapper.selectNow(Integer.parseInt(userId));
        BorrowList bl = new BorrowList(product, userId, newBlist == null ? null : newBlist.getBorrStatus());
        //保存借款信息
        borrowListMapper.insertSelective(bl);
        //查询当前用户冗余信息
        PhoneInfo info = phoneInfoMapper.selectNow(userId);
        if (info != null) {
            info.setBorrId(bl.getId());
        }
        phoneInfoMapper.updateByPrimaryKey(info);
        result.setCode(StateCode.SUCCESS_CODE);
        result.setInfo(StateCode.SUCCESS_MSG);
        //新建借款记录成功
        return result;
    }

    /**
     * 插入身份证正面信息
     *
     * @return
     */
    public NoteResult insertCardInfoz(String per_id, String card_num, String name,
                                      String sex, String nation, String birthday,
                                      String address, String card_byte, String head_byte, String description) {
        // 构建结果对象，默认201 失败
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        try {
            // 验证用户是否已经上传过身份证
            Person p = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
            if (p.getCardNum() != null && !card_num.equals(p.getCardNum())) {
                // 如果用户已经上传 并且跟当前上传的身份证号不一致 返回失败
                result.setInfo("请上传本人身份证");
                return result;
            }
            // 验证库里身份证是否对应该per_id
            Integer id = personMapper.selectByCardNum(card_num);

            // 此身份证库中已有，但不是这个per_id下的
            if (id != null && !p.getId().equals(id)) {
                // 清除该用户已上传的身份证信息
                DFSUtil.deleteFile(dfsUrl, p.getCardPhotoz());
                DFSUtil.deleteFile(dfsUrl, p.getCardPhotof());
                DFSUtil.deleteFile(dfsUrl, p.getCardPhotod());
                p.setCardPhotoz("");
                p.setCardPhotof("");
                p.setCardPhotod("");
                personMapper.updateByPrimaryKey(p);

                // 返回错误信息
                result.setInfo("该身份证已认证，无法重复认证");
                return result;
            }

//            屏蔽保存图片的代码(2018-05-16)
//            // 存正面图片
//            String pathz = DFSUtil.uploadByteArray(dfsUrl, decoder.decodeBuffer(card_byte), "jpg");
//            // 如果存图片失败 直接返回错误
//            if (StringUtils.isEmpty(pathz)) {
//                result.setInfo("图片系统出现一点小问题");
//                return result;
//            }
//
//            // 存头像图片
//            String pathd = DFSUtil.uploadByteArray(dfsUrl, decoder.decodeBuffer(head_byte), "jpg");
//            if (StringUtils.isEmpty(pathd)) {
//                result.setInfo("图片系统出现一点小问题");
//                return result;
//            }

//            p.setCardPhotoz(pathz);
//            p.setCardPhotod(pathd);
            p.setCardNum(card_num);
            p.setName(name);
            p.setSex(sex);
            p.setNation(nation);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(birthday);
            p.setBirthday(date);
            p.setAddress(address);
            personMapper.updateByPrimaryKeySelective(p);
            result = riskService.createBpmNode(per_id, NodeConstant.CARD_FRONT_NODE_ID, STATUS_BPM_Y, "");
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(FAIL_CODE);
            result.setInfo(FAIL_INFO);
            return result;
        }
        return result;
    }

    /**
     * 插入身份证反面信息
     *
     * @return
     */
    public NoteResult insertCardInfof(String per_id, String office, String start_date,
                                      String end_date, String card_byte, String description) {
        //构建结果对象，默认 201 失败
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        try {
            //card表中添加反面身份证信息
            Person p = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
            p.setOffice(office);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            p.setStartDate(sdf.parse(start_date));
            if (end_date != null && !"".equals(end_date)) {
                Date now = new Date();
                if ("永久".equals(end_date.trim()) || "长期".equals(end_date.trim())) {
                    // 如果是永久身份证 存
                    p.setEndDate(sdf.parse("2099-12-12"));
                } else if (now.getTime() - 24 * 60 * 60 * 1000 > sdf.parse(end_date).getTime()) {
                    // 已经过期
                    result.setCode(FAIL_CODE);
                    result.setInfo("该身份证已过期");
                    return result;
                } else {
                    p.setEndDate(sdf.parse(end_date));
                }
            }
//            屏蔽保存图片的代码(2018-05-16)
//            String pathf = DFSUtil.uploadByteArray(dfsUrl, decoder.decodeBuffer(card_byte), "jpg");
//            if (StringUtils.isEmpty(pathf)) {
//                result.setInfo("图片系统出现一点小问题");
//                return result;
//            }
//            p.setCardPhotof(pathf);
            personMapper.updateByPrimaryKeySelective(p);
            //新增认证流程节点明细
            result = riskService.createBpmNode(per_id, NodeConstant.CARD_BACK_NODE_ID, STATUS_BPM_Y, "");
            if(result.getCode().equals(SUCCESS_CODE)){
                //注册佣金埋点
                commissionRuleService.commissionCalculation(Integer.valueOf(per_id), Constants.TrackingStatus.REGISTER);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(FAIL_CODE);
            result.setInfo(FAIL_INFO);
            return result;
        }

        return result;
    }

    /**
     * 上传身份证，走风控
     */
    @Override
    public JSONObject cardOcrAndRisk(byte[] cardFile, byte[] headFile, String perId) {
        try {
            String path = DFSUtil.uploadByteArray(dfsUrl, cardFile, "jpg");
            if (StringUtils.isEmpty(path)) {
                logger.info(String.format("--------->图片上传失败 perId[%s], %s", perId, null == headFile ? "反面" : "正面"));
                JSONObject response = new JSONObject();
                response.put("code", "0001");
                response.put("info", "图片系统故障!");
                return response;
            }

            String headPath = (null != headFile) ? DFSUtil.uploadByteArray(dfsUrl, headFile, "jpg") : "";
            if (null != headFile && StringUtils.isEmpty(headPath)) {
                logger.info(String.format("--------->图片上传失败 perId[%s], 头像", perId));
                JSONObject response = new JSONObject();
                response.put("code", "0001");
                response.put("info", "图片系统故障!");
                return response;
            }

            Person person = new Person();
            person.setId(Integer.parseInt(perId));
            if (null == headFile) {
                person.setCardPhotof(path);
            }else {
                person.setCardPhotoz(path);
                person.setCardPhotod(headPath);
            }
            personMapper.updateByPrimaryKeySelective(person);

            logger.info(String.format("---->调用风控 身份证ocr 入参【%s】", path));
            String response = new OCRAPIAPIClient().cardOCR(path);
            logger.info(String.format("---->调用风控 身份证ocr 返回【%s】", response));
            return JSONObject.parseObject(response);
        }catch (Exception e) {
            e.printStackTrace();
            JSONObject response = new JSONObject();
            response.put("code", "9999");
            response.put("info", "系统异常!");
            return response;
        }
    }

    /**
     * 插入个人信息
     *
     * @param per_id         用户ID @param qq_num QQ号  @param email 邮箱
     * @param usuallyaddress 常用地址 @param education 学历
     * @param marry          婚姻状况  @param getchild 生育状况
     * @param profession     职业 @param monthlypay 月薪
     * @param business       单位名 @param busi_province 单位所在省
     * @param busi_city      单位所在市 @param busi_address 单位详细地址
     * @param busi_phone     单位电话 @param relatives 亲属关系
     * @param relatives_name 亲属名字 @param rela_phone 亲属联系方式
     * @param society        社会关系 @param soci_phone 社会联系方式
     * @param society_name   社会关系名字
     * @return
     */
    public NoteResult insertPrivateInfo(String per_id, String qq_num, String email, String usuallyaddress,
                                        String education, String marry, String getchild, String profession, String monthlypay, String business,
                                        String busi_province, String busi_city, String busi_address, String busi_phone, String relatives,
                                        String relatives_name, String rela_phone, String society, String soci_phone, String society_name, String tokenKey, String device) {

        //构建结果对象，默认 201 失败
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);

        // 幂等操作
        if (!StringUtils.isEmpty(jedisCluster.get(RedisConst.PR_KEY + per_id))) {
            logger.error("直接返回，重复数据private表");
            result.setInfo("数据重复！");
            return result;
        }

        String setNX = jedisCluster.set(RedisConst.PR_KEY + per_id, per_id, "NX", "EX", 60 * 2);
        if (!"OK".equals(setNX)) {
            logger.error("直接返回，重复数据private表");
            result.setInfo("数据重复！");
            return result;
        }

        // 2017-07-03 选择联系人超长 从后面截取10位
        if (relatives_name.length() > 10) {
            relatives_name = relatives_name.substring(relatives_name.length() - 10, relatives_name.length() - 1);
        }

        if (society_name.length() > 10) {
            society_name = society_name.substring(society_name.length() - 10, society_name.length() - 1);
        }

        //检查该per_id下是否有个人信息
        Private p = privateMapper.selectByPerId(Integer.valueOf(per_id));
        int i;
        if (p == null) {
            //插入个人信息
            p = new Private();
            p.setPerId(Integer.valueOf(per_id));
            p.setQqNum(qq_num);
            p.setEmail(email);
            p.setUsuallyaddress(usuallyaddress);
            p.setEducation(education);
            p.setMarry(marry);
            p.setGetchild(getchild);
            p.setProfession(profession);
            p.setMonthlypay(monthlypay);
            p.setBusiness(business);
            p.setBusiProvince(busi_province);
            p.setBusiCity(busi_city);
            p.setBusiAddress(busi_address);
            p.setBusiPhone(busi_phone);
            p.setRelatives(relatives);

            p.setRelativesName(relatives_name);
            p.setRelaPhone(rela_phone);
            p.setSociety(society);
            p.setSociPhone(soci_phone);

            p.setSocietyName(society_name);

            i = privateMapper.insertSelective(p);
        } else {

            p.setPerId(Integer.valueOf(per_id));
            p.setQqNum(qq_num);
            p.setEmail(email);
            p.setUsuallyaddress(usuallyaddress);
            p.setEducation(education);
            p.setMarry(marry);
            p.setGetchild(getchild);
            p.setProfession(profession);
            p.setMonthlypay(monthlypay);
            p.setBusiness(business);
            p.setBusiProvince(busi_province);
            p.setBusiCity(busi_city);
            p.setBusiAddress(busi_address);
            p.setBusiPhone(busi_phone);
            p.setRelatives(relatives);
            p.setRelativesName(relatives_name);
            p.setRelaPhone(rela_phone);
            p.setSociety(society);
            p.setSociPhone(soci_phone);
            p.setSocietyName(society_name);
            p.setUpdateDate(new Date());

            i = privateMapper.updateByPrimaryKey(p);
        }

        //插入个人信息成功    走风控 模型
        if (i > 0) {
            result = riskService.javaCheckRisk(per_id, email, tokenKey, device);
        }
        return result;
    }

    /**
     * 获取自己的手机号，及运营商展示
     *
     * @param per_id 用户ID
     * @return
     */
    public NoteResult getPhoneInfo(String per_id) {
        //构建结果对象，默认 201 失败
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
        if (person != null) {
            result.setCode(SUCCESS_CODE);
            result.setInfo(SUCCESS_INFO);
            result.setData(person.getPhone());
        }
        return result;
    }

    /**
     * 获取用户本地的手机通讯录名单
     *
     * @param per_id     用户ID
     * @param phone_list 用户本地的手机通讯录名单
     * @return
     */
    public NoteResult getPhoneList(String per_id, String phone_list) {
        // 构建结果对象，默认 201 失败
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        String newStr = phone_list;
        JSONArray newList = JSONArray.parseArray(newStr);
        int size = newList.size();
        Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
        String path;
        if (StringUtils.isEmpty(person.getContactUrl())) {
            logger.info("getPhoneList---第一次上传");
            // 文件存储方案
            path = DFSUtil.uploadString(dfsUrl, newStr);
            if (StringUtils.isNotEmpty(path)) {
                person.setContactUrl(path);
                person.setContactNum(size);
            }
        } else {
            logger.info("getPhoneList---重新上传");
            String oldPath = person.getContactUrl();
            String contacts = DFSUtil.downloadString(oldPath);
            // 原来有联系人 合并 + 去重
            JSONArray oldList = JSONArray.parseArray(contacts);
            JSONArray finalList = AddJsonarray.delRepeatIndexidJ(JSONArray.parseArray((AddJsonarray.joinJSONArray(newList, oldList))));
            path = DFSUtil.uploadString(dfsUrl, JSONObject.toJSONString(finalList));
            if (StringUtils.isNotEmpty(path)) {
                person.setContactUrl(path);
                person.setContactNum(size);
                //删除原来的文件
                if (StringUtils.isNotEmpty(oldPath)) {
                    DFSUtil.deleteFile(dfsUrl, oldPath);
                }
            }
        }
        //更新person表  联系人目录
        personMapper.updateByPrimaryKeySelective(person);
        //调用风控通讯录
        PersonAuthDto personAuthDto = new PersonAuthDto();
        personAuthDto.setIdCard(person.getCardNum());
        personAuthDto.setName(person.getName());
        personAuthDto.setPhone(person.getPhone());
        personAuthDto.setCurrentContacts(newStr);
        personAuthDto.setContactPath(path);
        FeatureDto featureDto = new FeatureDto();
        featureDto.setOrgCodeList("cts");
        featureDto.setNodeCode(NodeConstant.CONTACT_NODE);
        featureDto.setWhiteTag(0);
        logger.info("风控个人认证通讯录请求参数 personAuthDto:" + JSONObject.toJSONString(personAuthDto) + ",featureDto:" + JSONObject.toJSONString(featureDto));
        String response;
        try {
            ContactsAPI contantsApi = new ContactsAPIClient();
            response = contantsApi.runContactsCetifiction(Integer.parseInt(productId), personAuthDto, featureDto);
        } catch (Exception e) {
            result.setData(GUID_WRONG);
            logger.error("-------风控个人认证通讯录抛出异常\n" + e);
            return result;
        }
        logger.info("风控个人认证通讯录返回结果为----------------------response" + response);
        if (response == null) {
            result.setData(GUID_WRONG);
            logger.error("-------风控个人认证通讯录dubbo服务调用失败" + response);
            return result;
        }
        JSONObject obj = JSONObject.parseObject(response);
        //返回的报文中
        String code = obj.getString("code");
        String message = obj.getString("msg");
        //每次上传通讯录成功  更新缓存里 节点为7的节点
        BpmNode node = new BpmNode();
        node.setPerId(Integer.valueOf(per_id));
        node.setNodeId(NodeConstant.CONTACT_NODE_ID);
        node.setUpdateDate(new Date());
        if (RISK_SUCCESS_CODE.equals(code)) {
            node.setNodeStatus(STATUS_BPM_Y);
            manuallyReview(per_id, 1, 2, "");
            result.setCode(SUCCESS_CODE);
            result.setInfo(SUCCESS_INFO);
        } else if (CodeReturn.JuLiXinCode.RC_RULE.equals(code)) {
            node.setNodeStatus(STATUS_BPM_Y);
            manuallyReview(per_id, 1, 1, code + message);
            result.setCode(SUCCESS_CODE);
            result.setInfo(SUCCESS_INFO);
        } else if (CodeReturn.JuLiXinCode.RISK_FAIL_CODE.equals(code)) {//风控结果为拒绝
            node.setNodeStatus(STATUS_BPM_FAIL);
//            manuallyReview(per_id, 1, 1, code + message);
            result.setCode(BPM_UNDO_CODE);
            result.setInfo(BPM_UNDO_INFO);
            result.setData("r");
        } else {
            throw new DataAccessException("风控远程接口出现异常") {
                private static final long serialVersionUID = 4693491283309191722L;
            };
        }
        //更新节点
        riskService.createBpmNode(per_id, NodeConstant.CONTACT_NODE_ID, node.getNodeStatus(), message);

        return result;
    }

    /**
     * 认证完成，修改用户、流程、借款状态,触发人工审核，分配审核人等操作
     *
     * @param per_id 用户ID
     * @return
     */
    public NoteResult bpmFinish(String per_id) {
        logger.info("bpmFinish:" + "进入finish");
        // 结果对象
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        try {
            //人工审核新改动，改成待签约之前先判断person的状态
            Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
            logger.info("person:" + JSONObject.toJSONString(person));

            BorrowList borr = borrowListMapper.selectNow(Integer.valueOf(per_id));

            setReviewer(borr.getId(), "");

            if (StringUtils.isEmpty(person.getIsManual()) || "4".equals(person.getIsManual())) {
                //null或者4  不是非人工审核  按原来流程走
                logger.info("bpmFinish：" + "非人工审核");
                // 修改借款表的状态，把申请中改为待签约
                borr.setBorrStatus(STATUS_WAIT_SIGN);
                int k = borrowListMapper.updateByPrimaryKeySelective(borr);
                if (k > 0) {
                    logger.info("借款状态由申请中改为待签约：borr:" + JSONObject.toJSONString(borr));
                    result.setCode(SUCCESS_CODE);
                    result.setInfo(SUCCESS_INFO);
                }

            } else {
                //此用户为人工审核用户  插入一条人工审核表的记录  借款状态不更改
                logger.info("bpmFinish：" + "人工审核");
                borr.setIsManual(Integer.valueOf(person.getIsManual()));
                int k = borrowListMapper.updateByPrimaryKeySelective(borr);
                if (k > 0) {
                    result.setCode(NOW_BORROW_CODE);//202
                    result.setInfo("人工审核");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new NoteResult(FAIL_CODE, FAIL_INFO);
        }
        return result;
    }


    /**
     * 合同签约，状态改为已签约，添加签约时间
     *
     * @param per_id  用户ID
     * @param borr_id 合同id
     * @return
     */
    @Override
    public NoteResult signingBorrow(String per_id, String borr_id) {
        //构建结果对象，默认201 失败
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        try {
            //borr_id查询到用户当前的借款表
            BorrowList borrowList = borrowListMapper.selectByPrimaryKey(Integer.valueOf(borr_id));

            Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));

            //把当前借款表的借款状态改为已签约,添加签约时间
            if (!STATUS_WAIT_SIGN.equals(borrowList.getBorrStatus())) {
                //如果借款表的状态不为待签约，不能签约
                result.setCode(FAIL_CODE);
                result.setInfo(FAIL_INFO);
                return result;
            }
            borrowList.setBorrStatus(STATUS_SIGNED);
            long time = System.currentTimeMillis();
            Date date = new Date(time);
            borrowList.setMakeborrDate(date);
            borrowList.setUpdateDate(date);
            int i = borrowListMapper.updateByPrimaryKeySelective(borrowList);
            if (i > 0) {
                //更改成功
                //不自动放款 还按照原来流程走
                setReviewer(Integer.valueOf(borr_id), "");
                //签约成功以后调机器人审核
                // TODO 暂时关闭机器人审核，待百可录机器人打开后再开启此代码
                signRobot(borrowList);
                // 发站内信
                String name = person.getName();
                String money = borrowList.getBorrAmount().toString();
                //String term = borrowList.getTermNum().toString();
                //String params =name+","+money+","+term;
                String params = name + "," + money;
                userService.setMessage(per_id, "1", params);
                result.setCode(SUCCESS_CODE);
                result.setInfo(SUCCESS_INFO);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(FAIL_CODE);
            result.setInfo(FAIL_INFO);
            return result;
        }
        return result;
    }

    /**
     * 取消借款申请。判断合同状态，在申请中的合同才能取消借款申请。
     *
     * @param per_id  用户ID
     * @param borr_id 合同id
     * @return
     */
    public NoteResult cancelAskBorrow(String per_id, String borr_id) {
        //构建结果对象，默认201 失败
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        try {
            //根据borr_id获取借款表
            BorrowList borrowList = borrowListMapper.selectByPrimaryKey(Integer.valueOf(borr_id));
            String status = borrowList.getBorrStatus();
            if (STATUS_APLLY.equals(status) || STATUS_WAIT_SIGN.equals(status)) {
                borrowList.setBorrStatus(STATUS_CANCEL);
                long time = System.currentTimeMillis();
                Date date = new Date(time);
                borrowList.setUpdateDate(date);
                int i = borrowListMapper.updateByPrimaryKeySelective(borrowList);
                //取消借款申请成功
                if (i > 0) {
                    result.setCode(SUCCESS_CODE);
                    result.setInfo(SUCCESS_INFO);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new NoteResult(FAIL_CODE, FAIL_INFO);
        }
        return result;
    }

    /**
     * 风控五条规则触碰更新人工审核
     *
     * @param perId
     * @param isManual 触碰的节点
     */

    public boolean manuallyReview(String perId, int type, int isManual, String description) {
        logger.info("manuallyReview start: per_id = " + perId + "isManual = " + isManual);
        Person person = personMapper.selectByPrimaryKey(Integer.valueOf(perId));

        if (person == null) {
            return false;
        }

        //查出person原状态  1，通讯录人工审核 2，聚信立审核  3，都有 4 都没有
        String status = person.getIsManual();
        if (StringUtils.isEmpty(status)) {
            status = "4";//没有默认为4
        }

        try {
            //先判断isManual  审核还是解除  1审核，2解除
            if (isManual == 1) {//审核,判断是通讯录审核还是聚信立审核
                if (type == 1) {//通讯录审核
                    if ("1".equals(status)) {//原状态为1，状态不变，desc覆盖
                        person.setDescription(description);
                        int i = personMapper.updateByPrimaryKeySelective(person);
                        if (i > 0) {
                            logger.info("isManual=1,type=1,status=1,更新desc");
                            return true;
                        } else {
                            return false;
                        }
                    } else if ("2".equals(status)) {//原状态为2，改状态为3，desc前插
                        person.setIsManual("3");
                        String desc = person.getDescription();
                        desc = description + desc;
                        person.setDescription(desc);
                        int i = personMapper.updateByPrimaryKeySelective(person);
                        if (i > 0) {
                            logger.info("isManual=1,type=1,status=2,更新状态及desc");
                            return true;
                        } else {
                            return false;
                        }
                    } else if ("3".equals(status)) {//原状态为3，状态不变，desc前覆盖
                        String desc = person.getDescription();
                        desc = description + "&" + desc.split("&")[1];
                        person.setDescription(desc);
                        int i = personMapper.updateByPrimaryKeySelective(person);
                        if (i > 0) {
                            logger.info("isManual=1,type=1,status=3,更新desc");
                            return true;
                        } else {
                            return false;
                        }
                    } else {//原状态为4，改状态为1，desc新增
                        person.setIsManual("1");
                        person.setDescription(description);
                        int i = personMapper.updateByPrimaryKeySelective(person);
                        if (i > 0) {
                            logger.info("isManual=1,type=1,status=4,更新desc");
                            return true;
                        } else {
                            return false;
                        }
                    }
                } else {//聚信立审核
                    if ("1".equals(status)) {//原状态为1，改状态为3，desc追加
                        person.setIsManual("3");
                        String desc = person.getDescription();
                        person.setDescription(desc + "&" + description);
                        int i = personMapper.updateByPrimaryKeySelective(person);
                        if (i > 0) {
                            logger.info("isManual=1,type=2,status=1,更新status及desc");
                            return true;
                        } else {
                            return false;
                        }
                    } else if ("2".equals(status)) {//desc覆盖
                        person.setDescription("&" + description);
                        int i = personMapper.updateByPrimaryKeySelective(person);
                        if (i > 0) {
                            logger.info("isManual=1,type=2,status=2,更新desc");
                            return true;
                        } else {
                            return false;
                        }
                    } else if ("3".equals(status)) {//desc后覆盖
                        person.setDescription("&" + description);
                        int i = personMapper.updateByPrimaryKeySelective(person);
                        if (i > 0) {
                            logger.info("isManual=1,type=2,status=3,更新desc");
                            return true;
                        } else {
                            return false;
                        }
                    } else {//原状态为4，改状态为2，desc新增
                        person.setIsManual("2");
                        person.setDescription("&" + description);
                        int i = personMapper.updateByPrimaryKeySelective(person);
                        if (i > 0) {
                            logger.info("isManual=1,type=2,status=4,更新status及desc");
                            return true;
                        } else {
                            return false;
                        }
                    }

                }

            } else {//解除，判断是通讯录解除审核还是聚信立解除
                if (type == 1) {//通讯录解除
                    if ("1".equals(status)) {//原状态为1，改为4，desc清空
                        person.setIsManual("4");
                        person.setDescription("");
                        int i = personMapper.updateByPrimaryKeySelective(person);
                        if (i > 0) {
                            logger.info("isManual=2,type=1,status=1,更新status及desc");
                            return true;
                        } else {
                            return false;
                        }

                    } else if ("3".equals(status)) {//原状态为3，改为2，desc前清空
                        person.setIsManual("2");
                        String desc = person.getDescription();
                        person.setDescription("&" + desc.split("&")[1]);
                        int i = personMapper.updateByPrimaryKeySelective(person);
                        if (i > 0) {
                            logger.info("isManual=2,type=1,status=3,更新status及desc");
                            return true;
                        } else {
                            return false;
                        }
                    }

                } else {//聚信立解除
                    if ("2".equals(status)) {//原状态为2，改为4，desc清空
                        person.setIsManual("4");
                        person.setDescription("");
                        int i = personMapper.updateByPrimaryKeySelective(person);
                        if (i > 0) {
                            logger.info("isManual=2,type=2,status=2,更新status及desc");
                            return true;
                        } else {
                            return false;
                        }

                    } else if ("3".equals(status)) {//原状态为3，改为1，desc后清空
                        person.setIsManual("1");
                        String desc = person.getDescription();
                        person.setDescription(desc.split("&")[0]);
                        int i = personMapper.updateByPrimaryKeySelective(person);
                        if (i > 0) {
                            logger.info("isManual=2,type=2,status=3,更新status及desc");
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 获取签约界面信息
     *
     * @param per_id
     * @return
     */
    @Override
    public NoteResult getSignInfo(String per_id, String device, String token) {
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        SignInfo signInfo = productMapper.getSignInfo(per_id);
        if (null != signInfo) {
            signInfo.setPerId(per_id);
            signInfo.setDevice(device);
            signInfo.setToken(token);
            logger.info(JSONObject.toJSONString(signInfo));
            result.setCode(SUCCESS_CODE);
            result.setData(JSONObject.toJSON(signInfo));
            result.setInfo(SUCCESS_INFO);
        }
        return result;
    }


    /**
     * 根据用户id查询姓名及身份证号
     *
     * @param per_id 用户id
     * @return
     */
    @Override
    public NoteResult getIDNumber(String per_id) {
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
        JSONObject data = new JSONObject();
        if (person != null) {
            data.put("name", person.getName());
            data.put("number", person.getCardNum());
            data.put("phone", person.getPhone());
        }
        if (!data.isEmpty()) {
            result.setCode(SUCCESS_CODE);
            result.setInfo(SUCCESS_INFO);
            result.setData(data);
        }
        return result;
    }

    /**
     * 查询所有省市信息
     *
     * @return
     */
    @Override
    public NoteResult getCity() {
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        try {
            String city = jedisCluster.get(RedisConst.CITY_KEY);
            if (StringUtils.isEmpty(city)) {

                logger.info("redis里没有");
                // 放在data里面的结果集
                List<JSONObject> list = new ArrayList<JSONObject>();
                // 所有省
                List<City> province = cityMapper.findByPid(0);
                // 遍历每个省
                for (City c : province) {
                    // 该省下面的所有市
                    List<City> cities = cityMapper.findByPid(c.getId());

                    // 把所有的市放入这个省对象
                    JSONObject pro = new JSONObject();
                    pro.put("name", c.getName());
                    pro.put("id", c.getId());
                    pro.put("child", cities);

                    // 将对象加到结果集
                    list.add(pro);
                }

                if (!list.isEmpty()) {

                    String cityForSet = JSONObject.toJSONString(list);
                    jedisCluster.set(RedisConst.CITY_KEY, cityForSet);
                    result.setCode(SUCCESS_CODE);
                    result.setInfo(SUCCESS_INFO);
                    result.setData(list);
                }
            } else {
                logger.info("redis里有");
                result.setCode(SUCCESS_CODE);
                result.setInfo(SUCCESS_INFO);
                List<JSONObject> list2 = (List<JSONObject>) JSONObject.parse(city);
                result.setData(list2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new NoteResult(FAIL_CODE, FAIL_INFO);
        }
        return result;
    }

    /**
     * 人脸识别页面   获取头像照片或身份证正面照
     *
     * @param per_id
     * @return
     */
    @Override
    public NoteResult getCardz(String per_id) {
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        try {
            Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
            String cardName = person.getName();
            String cardNum = person.getCardNum();
            String path = person.getCardPhotod();
            result.setCode(SUCCESS_CODE);
            // 2017.04.25更改 info换成姓名+身份证
            result.setInfo(cardName + "," + cardNum);
            result.setData(path);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            result.setCode(FAIL_CODE);
            result.setInfo(FAIL_INFO);
            return result;
        }
        return result;
    }

    /**
     * 获取姓名及手机号
     *
     * @param per_id
     * @return
     */
    @Override
    public NoteResult getNamePhone(String per_id) {
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        try {
            Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
            int unread = msgMapper.selectUnread(per_id);
            JSONObject obj = new JSONObject();
            if (unread > 0) {
                obj.put("msg", "y");
            } else {
                obj.put("msg", "n");
            }
            obj.put("phone", person.getPhone());


            if (person.getName() == null) {
                obj.put("name", "未知");
            } else {
                obj.put("name", person.getName());
            }
            result.setCode(SUCCESS_CODE);
            result.setInfo(SUCCESS_INFO);


            //获取关注我们的提示信息,数据库enabled_flag字段为y时表示开启
            String followUsStr = personMapper.getFollowUsInfo();
            //将关注我们的提示信息放入obj中
            if (followUsStr != null && followUsStr.length() > 0) {
                //1表示显示关注我们
                obj.put("isShow", "1");
                obj.put("followUsStr", followUsStr);
            } else {
                obj.put("isShow", "0");
                obj.put("followUsStr", "");
            }
            result.setData(obj);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(FAIL_CODE);
            result.setInfo(FAIL_INFO);
            return result;
        }
        return result;
    }

    /**
     * 获取首页滚动条显示内容
     *
     * @param per_id
     * @return
     */
    @Override
    public ResponseDo<Map<String, Object>> getRolling(String per_id, String device) {
        ResponseDo<Map<String, Object>> responseDo = ResponseDo.newSuccessDo();
        //获取滚动条信息
        List<String> rolling = codeValueMapper.getMeaning("rolling", "rolling");
        //根据手机型号获取最大估价
        String amount = codeValueMapper.getMeaningByTypeCode("valuation", device);
        //获取联系我们中的工作时间
        String workTime = codeValueMapper.getMeaningByTypeCode("work_time", "work_time");
        //获取banner图片
        Example example = new Example(Banner.class);
        example.createCriteria().andEqualTo("path", device).andEqualTo("status", 1);
        List<Banner> banners = bannerMapper.selectByExample(example);
        Map<String, Object> map = new HashMap<>(16);
        map.put("rolling", rolling);
        map.put("amount", amount);
        map.put("serviceCall", serviceCall);
        map.put("workTime", workTime);
        map.put("workStart", workStart);
        map.put("workEnd", workEnd);
        map.put("banner", banners);
        responseDo.setData(map);
        return responseDo;
    }

    @Override
    public String verifyTokenId(String per_id, String token) {
        try {
            String loginTokenId = personMapper.getTokenId(per_id);
            if (token.equals(loginTokenId)) {
                return SUCCESS_CODE;
            } else {
                return FAIL_CODE;
            }
        } catch (Exception e) {
            return FAIL_CODE;
        }

    }

    /**
     * 2017.03.27---APP还款页面信息
     * @param per_id
     * @return
     */
    @Override
    public NoteResult repayInfo(String per_id) {
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);

        try {
            //生成订单号
            String serial = SerialNumUtil.createByType("JHS05");
            String company = "融合融资租赁（上海）有限公司";
            Person mode = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
            JSONObject obj = (JSONObject) JSONObject.toJSON(mode);
            obj.put("serial", serial);
            obj.put("company", company);
            result.setCode(SUCCESS_CODE);
            result.setInfo(SUCCESS_INFO);
            result.setData(obj);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(FAIL_CODE);
            result.setInfo("系统错误");
            return result;
        }
        return result;
    }

    /**
     * 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
     * @param path
     * @return
     */
    public String GetImageStr(String path) {//
        InputStream in;
        byte[] data = null;
        //读取图片字节数组
        try {
            in = new FileInputStream(path);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        //返回Base64编码过的字节数组字符串
        return encoder.encode(data);
    }

    /**
     * 对字节数组字符串进行Base64解码并生成图片
     * @param imgStr
     * @param path
     * @return
     */
    public boolean GenerateImage(String imgStr, String path) {
        //图像数据为空
        if (imgStr == null)
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; ++i) {
                //调整异常数据
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            //生成jpeg图片
            OutputStream out = new FileOutputStream(path);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 对字节数组字符串进行Base64解码并生成图片
     * @param b
     * @param path
     * @return
     */
    public boolean GenerateImage(byte[] b, String path) {
        // 图像数据为空
        if (b.length == 0)
            return false;
        try {

            for (int i = 0; i < b.length; ++i) {
                // 调整异常数据
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            // 生成jpeg图片
            OutputStream out = new FileOutputStream(path);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否可以绑卡
     * @param per_id
     * @return
     */
    @Override
    public NoteResult canBinding(String per_id) {
        NoteResult result = new NoteResult(FAIL_CODE, "请先完成认证再绑定银行卡");

        BpmNode node = bpmNodeMapper.selectByPerNode(Integer.valueOf(per_id), NodeConstant.PERSON_RISK_NODE_ID);

        if (node != null) {
            result.setCode(SUCCESS_CODE);
            result.setInfo(SUCCESS_INFO);
        }

        return result;
    }

    @Override
    public NoteResult saveVerifyPhoto(byte[] bytes, String per_id) {
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        try {
            Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));

            String pathV = DFSUtil.uploadByteArray(dfsUrl, bytes, "jpg");
            // 如果存图片失败 直接返回错误
            if (StringUtils.isEmpty(pathV)) {
                result.setInfo("图片系统出现一点小问题");
                return result;
            }
            person.setCardPhotov(pathV);

            int k = personMapper.updateByPrimaryKeySelective(person);

            if (k > 0) {
                result.setCode(SUCCESS_CODE);
                result.setInfo(SUCCESS_INFO);
            }
        } catch (Exception e) {

            e.printStackTrace();
            result.setCode(FAIL_CODE);
            result.setInfo("系统错误");
            return result;
        }
        return result;
    }

    @Override
    public NoteResult juxinliInfo() {

        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);

        try {
            InputStream is = getClass().getResourceAsStream("/../../userInfo.html");
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String fileString = "";
            String szTemp;

            while ((szTemp = bis.readLine()) != null) {
                fileString += szTemp;
            }
            bis.close();

            result.setCode(SUCCESS_CODE);
            result.setData(fileString);
            result.setInfo(SUCCESS_INFO);
        } catch (IOException e) {
            result.setInfo("系统异常或信息不存在");
            return result;
        }

        return result;
    }

    /**
     * 分配审核人方法
     * @param borrId
     * @param emp_num
     * @return
     */
    private int setReviewer(Integer borrId, String emp_num) {
        //幂等操作
        String redisResult = jedisCluster.get(RedisConst.REVIEW_KEY + borrId);
        logger.info(String.format("【setReviewer】jedisCluster get key: %s, result: %s, borrId: %s", RedisConst.REVIEW_KEY + borrId, redisResult, borrId));
        if (!StringUtils.isEmpty(redisResult)) {
            logger.error("直接返回，重复数据审核分配" + borrId);
            return 0;
        }

        String setNX = jedisCluster.set(RedisConst.REVIEW_KEY + borrId, borrId.toString(), "NX", "EX", 30 * 60);
        logger.info(String.format("【setReviewer】jedisCluster set key: %s, result: %s, borrId: %s", RedisConst.REVIEW_KEY + borrId, setNX, borrId));
        if (!"OK".equals(setNX)) {
            logger.error("直接返回，重复数据审核分配" + borrId);
            return 0;
        }

        //分配审核人之前先看有没有审核人 如果有 直接返回1
        if (reviewMapper.selectReview(borrId) > 0) {
            return 1;
        }
        //新增审核表记录
        Integer sum = reviewMapper.reviewSum();
        if (sum == null) {
            sum = 0;
        }
        //获得所有审核人的员工编号
        List<String> reviewerList = reviewersMapper.selectEmployNum();
        //给该borrowList设置审核人
        int turn = sum % reviewerList.size();

        Review review = new Review();
        review.setBorrId(borrId);
        review.setReviewType("1");
        //如果员工编号传空，自动分配
        if (StringUtils.isEmpty(emp_num)) {
            review.setEmployNum(reviewerList.get(turn));
        } else {
            review.setEmployNum(emp_num);
        }
        return reviewMapper.insertSelective(review);
    }

    public void signRobot(BorrowList borrowList) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        logger.info("当前首单审核时间为：" + currentHour + "时");
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        String beginTime = codeValueMapper.selectBaikeLuDate("baikelu_audit_begin_time");
        String endTime = codeValueMapper.selectBaikeLuDate("baikelu_audit_end_time");
        Boolean flagBeginTime = Integer.valueOf(sdf.format(new Date()).toString()) >= Integer.valueOf(beginTime);
        Boolean flagEndTime = Integer.valueOf(sdf.format(new Date()).toString()) <= Integer.valueOf(endTime);
        logger.info("flagBeginTime:" + flagBeginTime + "---------flagEndTime:" + flagEndTime);
        if (flagBeginTime && flagEndTime) {
            logger.info("进入机器人首单审核");
            String url = PropertiesReaderUtil.read("third", "robotUrl");
            Map<String, String> map = new HashMap<>();
            map.put("borrId", borrowList.getId().toString());
            HttpUrlPost.sendPost(url, map);
        }
    }
}
