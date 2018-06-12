package com.jhh.jhs.loan.app.app;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.app.LoanService;
import com.jhh.jhs.loan.api.app.RiskService;
import com.jhh.jhs.loan.api.app.UserService;
import com.jhh.jhs.loan.api.entity.BindCardVo;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.agreement.BorrowAgreement;
import com.jhh.jhs.loan.api.entity.capital.TradeVo;
import com.jhh.jhs.loan.api.loan.BankService;
import com.jhh.jhs.loan.api.loan.YsbCollectionService;
import com.jhh.jhs.loan.api.loan.YsbpayService;
import com.jhh.jhs.loan.app.common.constant.Admin;
import com.jhh.jhs.loan.common.constant.PayCenterChannelConstant;
import com.jhh.jhs.loan.common.util.CodeReturn;
import com.jhh.jhs.loan.common.util.Detect;
import com.jhh.jhs.loan.common.util.OcrUtil;
import com.jhh.jhs.loan.common.util.RedisConst;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.manager.CodeValue;
import com.jhh.pay.driver.service.TradeService;
import io.github.yedaxia.apidocs.ApiDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 借款模块
 *
 * @author xuepengfei
 */
@Controller
@RequestMapping("/loan")
public class LoanController extends BaseController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private BankService bankService;

    @Autowired
    private YsbCollectionService ysbCollectionService;

    @Autowired
    private YsbpayService ysbpayService;

    @Autowired
    private RiskService riskService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private UserService userService;

    @Autowired
    private JedisCluster jedisCluster;

    private static final String SUCCESS_CODE = CodeReturn.SUCCESS_CODE;

    private static final String FAIL_CODE = CodeReturn.FAIL_CODE;

    private static final String TOKEN_WRONG = CodeReturn.TOKEN_WRONG;

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);


    /**
     * 用户借款状态节点
     *
     * @param per_id 用户ID
     * @return
     */
    @ResponseBody
    @RequestMapping("/getBorrStatus")
    @ApiDoc(Admin.class)
    public String getBorrStatus(String per_id, String token) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (StringUtils.isEmpty(per_id) || StringUtils.isEmpty(token)) {
            return JSONObject.toJSONString(result);
        }
        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            result = loanService.getBorrStatus(per_id);
        } else {
            result.setCode(TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }
        return JSONObject.toJSONString(result);
    }

    /**
     * 生成借款记录
     *
     * @param per_id     用户ID
     * @param productId  产品ID
     * @param borrAmount 借款金额
     * @param token
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/borrowProduct", method = RequestMethod.POST)
    @ApiDoc(Admin.class)
    public ResponseDo<?> borrowProduct(@RequestParam String per_id, @RequestParam String productId, @RequestParam String borrAmount, @RequestParam String token) {
        logger.info("生成借款记录 userId =" + per_id + "\nproductId=" + productId + "\nborrAmount=" + borrAmount);
        ResponseDo<?> responseDo = new ResponseDo<>();
        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            responseDo = loanService.borrowProduct(per_id, productId, borrAmount);
        } else {
            responseDo.setCode(301);
            responseDo.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }


        return responseDo;
    }

    /**
     * 检查用户是否认证完成
     *
     * @param per_id 用户ID
     * @return
     */
    @ResponseBody
    @RequestMapping("/checkBpm")
    @ApiDoc(Admin.class)
    public String checkBpm(String per_id) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (StringUtils.isEmpty(per_id)) {
            return JSONObject.toJSONString(result);
        }
        result = riskService.checkBpm(per_id);
        return JSONObject.toJSONString(result);
    }


    /**
     * 插入身份证正面信息
     *
     * @param per_id      用户ID
     * @param card_num    身份证号
     * @param name        姓名
     * @param sex         性别
     * @param nation      国籍
     * @param birthday    生日
     * @param address     地址
     * @param card_byte   正面照片流
     * @param head_byte   正面头像照片流
     * @param description 备注
     * @param token
     * @return
     */
    @ResponseBody
    @RequestMapping("/insertCardInfoz")
    @ApiDoc(Admin.class)
    public String insertCardInfoz(String per_id, String card_num, String name,
                                  String sex, String nation, String birthday,
                                  String address, String card_byte, String head_byte, String description, String token) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");

        if (StringUtils.isEmpty(per_id) || StringUtils.isEmpty(card_num) || StringUtils.isEmpty(name) || StringUtils.isEmpty(sex)
                || StringUtils.isEmpty(nation) || StringUtils.isEmpty(birthday) || StringUtils.isEmpty(address)) {
            return JSONObject.toJSONString(result);
        }

        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            if (card_num.contains("*")) {
                //身份证号识别有误  重新扫描

            }
            result = loanService.insertCardInfoz(per_id, card_num, name, sex, nation,
                    birthday, address, card_byte, head_byte, description);
        } else {
            result.setCode(TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }


        return JSONObject.toJSONString(result);
    }


    /**
     * 插入身份证反面信息
     *
     * @param per_id      用户ID
     * @param office      发证机关
     * @param start_date  有效期开始时间
     * @param end_date    有效期结束时间
     * @param card_byte   反面照片流
     * @param description 备注
     * @param token
     * @return
     */
    @ResponseBody
    @RequestMapping("/insertCardInfof")
    @ApiDoc(Admin.class)
    public String insertCardInfof(String per_id, String office, String start_date,
                                  String end_date, String card_byte, String description, String token) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (StringUtils.isEmpty(per_id) || StringUtils.isEmpty(office) || StringUtils.isEmpty(start_date)) {

            return JSONObject.toJSONString(result);
        }

        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            result = loanService.insertCardInfof(per_id, office, start_date, end_date, card_byte, description);
        } else {
            result.setCode(TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }


        return JSONObject.toJSONString(result);
    }

    /**
     * 插入个人信息 并进行风控
     *
     * @param per_id         用户ID
     * @param qq_num         QQ号
     * @param email          邮箱
     * @param usuallyaddress 常用住址
     * @param education      教育程度
     * @param marry          婚姻状况
     * @param getchild       是否有孩
     * @param profession     专业
     * @param monthlypay     月薪
     * @param business       行业
     * @param busi_province  公司所在省
     * @param busi_city      公司所在市
     * @param busi_address   公司地址
     * @param busi_phone     公司电话
     * @param relatives      亲属关系
     * @param relatives_name 亲属姓名
     * @param rela_phone     亲属联系方式
     * @param society        社会关系
     * @param soci_phone     社会关系联系方式
     * @param society_name   社会关系姓名
     * @param token
     * @return
     */
    @ResponseBody
    @RequestMapping("/insertPrivateInfo")
    @ApiDoc(Admin.class)
    public String insertPrivateInfo(String per_id, String qq_num, String email, String usuallyaddress,
                                    String education, String marry, String getchild, String profession, String monthlypay, String business,
                                    String busi_province, String busi_city, String busi_address, String busi_phone, String relatives,
                                    String relatives_name, String rela_phone, String society, String soci_phone, String society_name, String token, String tokenKey, String device) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        logger.info(String.format("-------->提交个人信息param【per_id:%s, qq_num:%s, email:%s, usuallyaddress:%s, education:%s, marry:%s, getchild:%s, profession:%s, monthlypay:%s, business:%s, busi_province:%s, busi_city:%s, busi_address:%s, busi_phone:%s, relatives:%s, relatives_name:%s, rela_phone:%s, society:%s, soci_phone:%s, society_name:%s, token:%s, tokenKey:%s, device:%s】", per_id, qq_num, email, usuallyaddress, education, marry, getchild, profession, monthlypay, business, busi_province, busi_city, busi_address, busi_phone, relatives, relatives_name, rela_phone, society, soci_phone, society_name, token, tokenKey, device));

        if (per_id == null || "".equals(per_id.trim())
                ||
                qq_num == null || "".equals(qq_num.trim())
                ||
                email == null || "".equals(email.trim())
                ||
                usuallyaddress == null || "".equals(usuallyaddress.trim())
                ||
                education == null || "".equals(education.trim())
                ||
                marry == null || "".equals(marry.trim())
                ||
                getchild == null || "".equals(getchild.trim())
                ||
                profession == null || "".equals(profession.trim())
                ||
                monthlypay == null || "".equals(monthlypay.trim())
                ||
                business == null || "".equals(business.trim())
                ||
                busi_province == null || "".equals(busi_province.trim())
                ||
                busi_city == null || "".equals(busi_city.trim())
                ||
                busi_address == null || "".equals(busi_address.trim())
                ||
                busi_phone == null || "".equals(busi_phone.trim())
                ||
                relatives == null || "".equals(relatives.trim())
                ||
                relatives_name == null || "".equals(relatives_name.trim())
                ||
                rela_phone == null || "".equals(rela_phone.trim())
                ||
                society == null || "".equals(society.trim())
                ||
                soci_phone == null || "".equals(soci_phone.trim())
                ||
                society_name == null || "".equals(society_name.trim())) {

            return JSONObject.toJSONString(result);
        }

        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            result = loanService.insertPrivateInfo(per_id, qq_num, email, usuallyaddress, education, marry,
                    getchild, profession, monthlypay, business, busi_province,
                    busi_city, busi_address, busi_phone, relatives, relatives_name,
                    rela_phone, society, soci_phone, society_name, tokenKey, device);
        } else {
            result.setCode(TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }


        return JSONObject.toJSONString(result);
    }

    /**
     * 银行卡认证
     */
    @ResponseBody
    @RequestMapping("/insertBankInfo")
    @ApiDoc(Admin.class)
    public NoteResult insertBankInfo(@Valid BindCardVo vo, BindingResult br) {
        if (br.hasErrors()) {
            List<BindingResult> bindingResults = new ArrayList<>();
            bindingResults.add(br);
            Map<String, String> map = getResult(bindingResults);
            logger.info("-------------有参数为空" + map);
            return NoteResult.FAIL_RESPONSE("参数不能为空"+map.toString());
        }
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");

        // 判断验证码是否正确
        String valiCodeKey = new StringBuilder(RedisConst.VALIDATE_CODE).append(RedisConst.SEPARATOR).append(vo.getPhone()).toString();
        String valiCode = jedisCluster.get(valiCodeKey);
        if(org.apache.commons.lang.StringUtils.isEmpty(valiCode)){
            result.setInfo("请先获取验证码!");
            return result;
        }
        if(!PayCenterChannelConstant.PAY_CHANNEL_HLB_QUICK.equals(valiCode) && !vo.getValidateCode().equals(valiCode)){
            result.setInfo("验证码不正确!");
            return result;
        }
        // 判断当前默认付款渠道是否是合利宝快捷支付
        CodeValue codeValue = userService.selectDefaultRepayChannel();
        //TODO:临时解决方案 根据用户银行卡选择渠道路由
        TradeVo tradeVo = new TradeVo();
        tradeVo.setBankNum(vo.getBank_num());
        tradeVo = userService.chooseBankRouting(tradeVo);
        if(("0".equals(codeValue.getCodeCode()) && PayCenterChannelConstant.PAY_CHANNEL_HLB_QUICK.equals(codeValue.getMeaning())) || PayCenterChannelConstant.PAY_CHANNEL_HLB_QUICK.equals(tradeVo.getPayChannel())){
            // 判断用户是否绑定了合利宝快捷支付
            Integer bindStatus = userService.queryQuickBind(vo.getBank_num());
            if(bindStatus != null){
                // 如果绑定则不需要进行绑卡
                vo.setValidateCode(null);
            }else{
                vo.setExtension("{\"userId\":\""+vo.getPer_id()+"\"}");
            }
        }else {
            vo.setValidateCode(null);
        }
        String verify = loanService.verifyTokenId(vo.getPer_id(), vo.getToken());
        if (SUCCESS_CODE.equals(verify)) {
            if (StringUtils.isEmpty(vo.getTokenKey())) {
                vo.setTokenKey("");
            }
            result = bankService.payCenterBindCard(vo);
        } else {
            result.setCode(TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }
        return result;
    }

    /**
     * 查询银行卡认证（前端不需要）
     *
     * @param per_id 用户ID
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryBankInfo")
    public String queryBankInfo(String per_id, String bank_num) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (StringUtils.isEmpty(per_id) || StringUtils.isEmpty(bank_num)) {
            return JSONObject.toJSONString(result);
        }
        result = bankService.queryContractId(per_id, bank_num);
        return JSONObject.toJSONString(result);
    }

    /**
     * 获取自己的手机号，及运营商展示
     *
     * @param per_id 用户ID
     * @return
     */
    @ResponseBody
    @RequestMapping("/getPhoneInfo")
    @ApiDoc(Admin.class)
    public String getPhoneInfo(String per_id) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (StringUtils.isEmpty(per_id)) {
            return JSONObject.toJSONString(result);
        }

        result = loanService.getPhoneInfo(per_id);


        return JSONObject.toJSONString(result);
    }

    /**
     * 获取用户本地的手机通讯录名单
     *
     * @param per_id     用户ID
     * @param phone_list 用户本地的手机通讯录名单
     * @return
     */
    @ResponseBody
    @RequestMapping("/getPhoneList")
    @ApiDoc(Admin.class)
    public String getPhoneList(String per_id, String phone_list) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (StringUtils.isEmpty(per_id) || StringUtils.isEmpty(phone_list)) {

            return JSONObject.toJSONString(result);
        }

        result = loanService.getPhoneList(per_id, phone_list);
        return JSONObject.toJSONString(result);
    }

    /**
     * 认证完成，修改用户、流程、借款状态
     *
     * @param per_id 用户ID
     * @return
     */
    @ResponseBody
    @RequestMapping("/bpmFinish")
    @ApiDoc(Admin.class)
    public String bpmFinish(String per_id, String token) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (StringUtils.isEmpty(per_id)) {
            return JSONObject.toJSONString(result);
        }

        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            result = loanService.bpmFinish(per_id);
        } else {
            result.setCode(TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }


        return JSONObject.toJSONString(result);
    }


    /**
     * 获取签约界面信息
     *
     * @return
     */
    @RequestMapping("/getSignPage")
    @ApiDoc(Admin.class)
    public String getSignPage(HttpServletRequest request) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        String per_id = request.getParameter("per_id");
        String token = request.getParameter("token");
        String device = request.getParameter("device");
        if (StringUtils.isEmpty(per_id) || StringUtils.isEmpty(token) || StringUtils.isEmpty(device)) {
            return JSONObject.toJSONString(result);
        }
        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            result = loanService.getSignInfo(per_id, device, token);
        } else {
            result.setCode(CodeReturn.FAIL_CODE);
            result.setInfo("失败");
        }
        logger.info(JSONObject.toJSONString(result.getData()));
        request.setAttribute("data", result);
        return "signPage";
    }

    /**
     * 验证token
     *
     * @param per_id
     * @param token
     * @return
     */
    @RequestMapping("/verifyToken")
    @ResponseBody
    @ApiDoc(Admin.class)
    public NoteResult verifyToken(String per_id, String token) {
        NoteResult result = new NoteResult();
        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            result.setCode(CodeReturn.SUCCESS_CODE);
        } else {
            result.setCode(CodeReturn.FAIL_CODE);
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
    @ResponseBody
    @RequestMapping("/signingBorrow")
    @Transactional()
    @ApiDoc(Admin.class)
    public String signingBorrow(String per_id, String borr_id, String token) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        String str = JSONObject.toJSONString(result);
        if (StringUtils.isEmpty(per_id) || StringUtils.isEmpty(borr_id) || StringUtils.isEmpty(token)) {
            return str;
        }

        String verify = loanService.verifyTokenId(per_id, token);
        if (!SUCCESS_CODE.equals(verify)) {
            result.setCode(CodeReturn.MD5_WRONG);
            result.setInfo("系统繁忙");
            return JSONObject.toJSONString(result);
        }

        result = riskService.checkBpm(per_id);
        if (!CodeReturn.BPM_FINISH_CODE.equals(result.getCode())) {
            result.setCode(CodeReturn.FAIL_CODE);
            return JSONObject.toJSONString(result);
        }

        result = loanService.signingBorrow(per_id, borr_id);
        return JSONObject.toJSONString(result);
    }



    /**
     * 取消借款申请。判断合同状态，在申请中的合同才能取消借款申请。
     *
     * @param per_id  用户ID
     * @param borr_id 合同id
     * @return
     */
    @ResponseBody
    @RequestMapping("/cancelAskBorrow")
    @ApiDoc(Admin.class)
    public String cancelAskBorrow(String per_id, String borr_id, String token) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (per_id == null || "".equals(per_id.trim())
                ||
                borr_id == null || "".equals(borr_id.trim())) {
            return JSONObject.toJSONString(result);
        }

        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            result = loanService.cancelAskBorrow(per_id, borr_id);
        } else {
            result.setCode(TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }


        return JSONObject.toJSONString(result);
    }

    /**
     * 根据用户id查询姓名及身份证号
     *
     * @param per_id 用户id
     * @return
     */
    @ResponseBody
    @RequestMapping("/getIDNumber")
    @ApiDoc(Admin.class)
    public String getIDNumber(String per_id, String token) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (per_id == null || "".equals(per_id.trim())) {
            return JSONObject.toJSONString(result);
        }

        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            result = loanService.getIDNumber(per_id);
        } else {
            result.setCode(TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }


        return JSONObject.toJSONString(result);
    }

    /**
     * 查询所有省市信息
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/getCity")
    @ApiDoc(Admin.class)
    public String getCity() {
        logger.info("***************1.0.1版本");
        NoteResult result = loanService.getCity();
        return JSONObject.toJSONString(result);
    }

    /**
     * 查询银行列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/getBankList")
    @ApiDoc(Admin.class)
    public String getBankList() {
        NoteResult result = bankService.getBankList();
        return JSONObject.toJSONString(result);
    }

    @RequestMapping(value = "getBankList", headers = "apiVersion=0.0.1")
    @ResponseBody
    public String getBankList2() {
        return "0.0.1版本测试";
    }

    /**
     * 获取当前节点的认证状态
     *
     * @param per_id  用户ID
     * @param node_id 节点编号
     * @return
     */
    @ResponseBody
    @RequestMapping("/getNodeStatus")
    @ApiDoc(Admin.class)
    public String getNodeStatus(String per_id, String node_id, String token) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (per_id == null || "".equals(per_id.trim())
                ||
                node_id == null || "".equals(node_id.trim())) {
            return JSONObject.toJSONString(result);
        }

        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            result = riskService.getNodeStatus(per_id, node_id);
        } else {
            result.setCode(TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }


        return JSONObject.toJSONString(result);
    }

    /**
     * 人脸识别页面   获取身份证正面照
     *
     * @param per_id
     * @return
     */
    @ResponseBody
    @RequestMapping("/getCardz")
    @ApiDoc(Admin.class)
    public String getCardz(String per_id) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (per_id == null || "".equals(per_id.trim())) {
            return JSONObject.toJSONString(result);
        }

        result = loanService.getCardz(per_id);


        return JSONObject.toJSONString(result);
    }

    /**
     * 获取用户姓名及手机号
     *
     * @param per_id
     * @return
     */
    @ResponseBody
    @RequestMapping("/getNamePhone")
    @ApiDoc(Admin.class)
    public String getNamePhone(String per_id, String token) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (per_id == null || "".equals(per_id.trim())) {
            return JSONObject.toJSONString(result);
        }

        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            result = loanService.getNamePhone(per_id);
        } else {
            result.setCode(TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }

        return JSONObject.toJSONString(result);
    }


    /**
     * 后台管理系统代扣接口
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/askCollect")
    @ApiDoc(Admin.class)
    public String askCollect(HttpServletRequest request) {
        StringBuffer info = new StringBuffer();
        InputStream is = null;
        String xing = "";
        JSONObject jsonobject;
        String response;
        try {
            is = request.getInputStream();
            BufferedInputStream buf = new BufferedInputStream(is);
            byte[] buffer = new byte[1024];
            int iRed;
            while ((iRed = buf.read(buffer)) != -1) {
                info.append(new String(buffer, 0, iRed, "UTF-8"));
            }
            xing = URLDecoder.decode(info.toString(), "UTF-8");
            jsonobject = JSONObject.parseObject(xing);
            JSONObject body = JSONObject.parseObject(jsonobject.getString("Body"));
            JSONObject head = JSONObject.parseObject(jsonobject.getString("Head"));
            String guid = head.getString("Guid");
            String content = body.getString("Content");

            JSONObject obj = JSONObject.parseObject(content);
            String borrNum = obj.getString("borrNum");
            String name = obj.getString("name");
            String idCardNo = obj.getString("idCardNo");
            String optAmount = obj.getString("optAmount");
            String bankId = obj.getString("bankId");
            String bankNum = obj.getString("bankNum");
            String serNo = obj.getString("serNo");
            String phone = obj.getString("phone");
            String description = obj.getString("description");
            String createUser = "";
            String collectionUser = "";
            if (Detect.notEmpty(obj.getString("createUser"))) {
                createUser = obj.getString("createUser");
            }
            if (Detect.notEmpty(obj.getString("collectionUser"))) {
                collectionUser = obj.getString("collectionUser");
            }

            NoteResult result = ysbCollectionService.askCollection(guid, borrNum, name, idCardNo,
                    optAmount, bankId, bankNum, phone, description, serNo, createUser, collectionUser);

            logger.info(JSONObject.toJSONString(result));
            head.put("RespCode", result.getCode());
            head.put("RespMessage", result.getInfo());
            jsonobject.put("Head", head);

            JSONObject resContentJSON = JSONObject.parseObject(content);
            if (result.getData() != null) {
                resContentJSON.put("serNo", result.getData());
            }
            String resContent = JSONObject.toJSONString(resContentJSON);
            body.put("Content", resContent);
            jsonobject.put("Body", body);
            logger.info(JSONObject.toJSONString(jsonobject));
            response = URLEncoder.encode(JSONObject.toJSONString(jsonobject), "utf-8");
        } catch (Exception e1) {

            e1.printStackTrace();
            return "参数错误";
        }

        return response;
    }

    /**
     * 后台管理系统查询订单接口接口
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryCollectStatus")
    @ApiDoc(Admin.class)
    public void queryCollectStatus() {
        ysbCollectionService.queryCollectStatus();
    }

    /**
     * 获取首页滚动信息与预估金额
     *
     * @param per_id
     * @return
     */
    @ResponseBody
    @RequestMapping("/getRolling")
    @ApiDoc(Admin.class)
    public ResponseDo<Map<String, Object>> getRolling(String per_id, String device, String token) {
        return loanService.getRolling(per_id, device);
    }

    /**
     * 获取聚信立协议
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/juxinliInfo")
    @ApiDoc(Admin.class)
    public String juxinliInfo(HttpServletRequest request) {
        NoteResult result = loanService.juxinliInfo();
        return JSONObject.toJSONString(result);
    }

    /**
     * 增加第三方接口调用次数接口
     *
     * @param per_id 用户id
     * @param type   身份证接口1/活体检测接口2
     * @param count  调用接口数
     * @param status 本次认证是否成功s/f
     * @return
     */
    @ResponseBody
    @RequestMapping("/addCount")
    @ApiDoc(Admin.class)
    public String addCount(String per_id, String type, String count, String status) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (per_id == null || "".equals(per_id.trim())
                ||
                type == null || "".equals(type.trim())
                ||
                count == null || "".equals(count.trim())
                ||
                status == null || "".equals(status.trim())) {

            return JSONObject.toJSONString(result);
        }

        result = riskService.addCount(per_id, type, count, status);


        return JSONObject.toJSONString(result);
    }

    /**
     * 还款H5页面跳转
     */
    @RequestMapping("/repayPage")
    public String repayPage(HttpServletRequest request) {
        String perId = request.getParameter("per_id");
        String amount = request.getParameter("amount");
        NoteResult result = loanService.repayInfo(perId);
        if (!StringUtils.isEquals(CodeReturn.SUCCESS_CODE, result.getCode())) {
            return "pay/pay";
        }
        JSONObject data = (JSONObject) result.getData();
        try {
            data.put("amount", StringUtils.isEmpty(amount) ? "0.01" : String.format("%.2f", Float.parseFloat(amount)));
            data.put("perId", perId);
        } catch (Exception e) {
            logger.info("----> 出错了：", e);
            return "pay/pay";
        }

        request.setAttribute("result", data);
        return "pay/pay";
    }

    /**
     * 还款页面信息接口
     *
     * @param per_id
     * @return
     */
    @ResponseBody
    @RequestMapping("/repayInfo")
    @ApiDoc(Admin.class)
    public String repayInfo(String per_id, String token) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (per_id == null || "".equals(per_id.trim())) {
            return JSONObject.toJSONString(result);
        }

        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            result = loanService.repayInfo(per_id);
        } else {
            result.setCode(TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }


        return JSONObject.toJSONString(result);
    }

    /**
     * APP用户主动提交还款
     *
     * @param per_id 用户ID
     * @param serial 唯一码
     * @param amount 还款金额
     * @param token
     * @return
     */
    @ResponseBody
    @RequestMapping("/AppRepay")
    @ApiDoc(Admin.class)
    public String AppRepay(String per_id, String serial, String amount, String token) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (per_id == null || "".equals(per_id.trim()) || serial == null || "".equals(serial.trim()) || amount == null
                || "".equals(amount.trim()) || token == null || "".equals(token.trim())) {
            return JSONObject.toJSONString(result);
        }

        String verify = loanService.verifyTokenId(per_id, token);
        if (SUCCESS_CODE.equals(verify)) {
            result = ysbpayService.AppRepay(per_id, serial, amount);
        } else {
            result.setCode(TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
        }


        return JSONObject.toJSONString(result);
    }

    /**
     * 身份证OCR
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/cardOcr", method = RequestMethod.POST)
    @ApiDoc(Admin.class)
    public String cardOcr(MultipartHttpServletRequest request) {

        String perId = request.getParameter("per_id");
        String token = request.getParameter("token");
        MultipartFile headFile = request.getFile("head");
        MultipartFile cardFile = request.getFile("card");
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (perId == null || "".equals(perId.trim()) || token == null || "".equals(token.trim())) {
            return JSONObject.toJSONString(result);
        }

        String verify = loanService.verifyTokenId(perId, token);
        if (!SUCCESS_CODE.equals(verify)) {
            result.setCode(TOKEN_WRONG);
            result.setInfo("您的账号已在另一台设备上登录，请重新登录");
            return JSONObject.toJSONString(result);
        }

        JSONObject response;
        try {
            response = loanService.cardOcrAndRisk(cardFile.getBytes(), headFile == null ? null : headFile.getBytes(), perId);
        }catch (Exception e) {
            e.printStackTrace();
            result.setInfo("身份证扫描异常,请稍候再试!");
            return JSONObject.toJSONString(result);
        }

        if ("0001".equals(response.getString("code"))) {
            result.setInfo("身份证扫描失败");
        } else if ("9999".equals(response.getString("code"))) {
            result.setInfo("身份证扫描异常,请稍候再试!");
        } else {
            result.setCode(SUCCESS_CODE);
            result.setInfo("身份证扫描成功");
            result.setData(response.getJSONObject("data"));
        }

        return JSONObject.toJSONString(result);
    }

    /**
     * 人脸识别
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/faceVerify", method = RequestMethod.POST)
    @ApiDoc(Admin.class)
    public String faceVerify(MultipartHttpServletRequest request) {

        String per_id = request.getParameter("per_id");
        String token = request.getParameter("token");
        MultipartFile image_best = request.getFile("image_best");
        MultipartFile image_env = request.getFile("image_env");
        String delta = request.getParameter("delta");
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (StringUtils.isEmpty(per_id) || StringUtils.isEmpty(token)) {
            return JSONObject.toJSONString(result);
        }

        try {

            String verify = loanService.verifyTokenId(per_id, token);
            if (SUCCESS_CODE.equals(verify)) {

                // 2017-06-16改动 先存人脸识别的照片
                byte[] bytes = image_env.getBytes();

                NoteResult save = loanService.saveVerifyPhoto(bytes, per_id);

                // 如果存图片没成功 直接返回失败
                if (SUCCESS_CODE.equals(save.getCode())) {
                    logger.info("存图片返回结果：" + JSONObject.toJSONString(save));
                    NoteResult cardInfo = this.loanService.getCardz(per_id);
                    String cardName = cardInfo.getInfo().split(",")[0];
                    String cardNum = cardInfo.getInfo().split(",")[1];
                    String fileUrl = (String) cardInfo.getData();

                    String response = OcrUtil.faceVerifyRaw(fileUrl, cardName, cardNum);
                    if ("201".equals(response)) {
                        result.setInfo("人脸识别发生错误");
                    } else if ("202".equals(response)) {
                        NoteResult addCount = riskService.addCount(per_id, "4", "1", "f");
                        if (CodeReturn.VERIFY_FAIL_CODE.equals(addCount.getCode())) {
                            //人脸3次失败  无法继续认证
                            return JSONObject.toJSONString(addCount);
                        }
                        result.setInfo("人脸识别失败，请重新认证！");
                    } else {
                        //raw成功
                        // 人脸识别 meglive
                        String megResponse = OcrUtil.faceVerifyMeg(image_best, image_env, cardName, cardNum, delta);
                        if ("200".equals(megResponse)) {
                            // 人脸识别两次全成功 调收费接口
                            // type = 4 人脸识别 count = 2 进行了2次对比 status = s 成功
                            riskService.addCount(per_id, "4", "2", "s");
                            result.setCode(SUCCESS_CODE);
                            result.setInfo("成功");
                        } else if ("202".equals(megResponse)) {
                            // 202 失败 但进行了2次对比
                            // 调收费接口 type = 4 人脸识别 count = 2 进行了2次对比 status
                            // = f 失败
                            NoteResult addCount = riskService.addCount(per_id, "4", "2", "f");
                            if (CodeReturn.VERIFY_FAIL_CODE.equals(addCount.getCode())) {
                                //人脸3次失败  无法继续认证
                                return JSONObject.toJSONString(addCount);
                            }
                            result.setInfo("人脸识别检测失败，请重新认证！");
                        } else if ("201".equals(megResponse)) {
                            // 201 返回error 没进行对比
                            result.setInfo("人脸识别检测发生错误");
                        }
                    }

                } else {// 图片没存成功
                    return JSONObject.toJSONString(new NoteResult(FAIL_CODE, "系统错误"));
                }

            } else {
                result.setCode(TOKEN_WRONG);
                result.setInfo("您的账号已在另一台设备上登录，请重新登录");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return JSONObject.toJSONString(new NoteResult(FAIL_CODE, "系统错误"));
        }

        return JSONObject.toJSONString(result);
    }


    /**
     * 用户银行卡列表
     *
     * @param per_id
     * @return
     */
    @ResponseBody
    @RequestMapping("/personBanks")
    @ApiDoc(Admin.class)
    public String personBanks(String per_id) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (per_id == null || "".equals(per_id.trim())) {

            return JSONObject.toJSONString(result);
        }

        result = bankService.personBanks(per_id);


        return JSONObject.toJSONString(result);
    }

    /**
     * 用户APP更换主副卡
     *
     * @param per_id   用户ID
     * @param bank_num 银行卡号
     * @return
     */
    @ResponseBody
    @RequestMapping("/changeBank")
    @ApiDoc(Admin.class)
    public String changeBank(String per_id, String bank_num) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (per_id == null || "".equals(per_id.trim()) || bank_num == null || "".equals(bank_num.trim())) {

            return JSONObject.toJSONString(result);
        }

        // 更换主副卡
        boolean change = bankService.changeBankStatus(per_id, bank_num);

        if (change) {// 更换成功
            result.setCode(SUCCESS_CODE);
            result.setInfo("更换主副卡成功");
        }


        return JSONObject.toJSONString(result);
    }

    /**
     * 用户是否可以绑定银行卡
     *
     * @param per_id
     * @return
     */
    @ResponseBody
    @RequestMapping("/canBinding")
    @ApiDoc(Admin.class)
    public String canBinding(String per_id) {
        NoteResult result = new NoteResult(FAIL_CODE, "参数错误");
        if (per_id == null || "".equals(per_id.trim())) {

            return JSONObject.toJSONString(result);
        }

        result = loanService.canBinding(per_id);


        return JSONObject.toJSONString(result);
    }

    /**
     * 芝麻认证
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/zhima")
    @ApiDoc(Admin.class)
    public String zhima(String per_id) {

        NoteResult result = new NoteResult();


        result = riskService.zhima(per_id);

        return JSONObject.toJSONString(result);
    }

//    /*
//        测试机器人数据接口
//     */
//    @ResponseBody
//    @RequestMapping("/testRobot")
//    public void testRobot() {
//        logger.info("测试机器人数据");
//        timerService.sendRobotData();
//    }

    /**
     * 单独查询订单状态接口
     *
     * @param serialNo 订单号
     * @return
     */
    @ResponseBody
    @RequestMapping("/orderStatus")
    @ApiDoc(Admin.class)
    public String orderStatus(String serialNo) {

        return JSONObject.toJSONString(ysbCollectionService.orderStatus(serialNo));
    }

    /**
     * 借款协议
     *
     * @param request
     * @param per_id
     * @return
     */
    @RequestMapping("/agreement")
    @ApiDoc(Admin.class)
    public String getAgreement(HttpServletRequest request, @RequestParam String per_id) {
        logger.info("借款协议请求参数 per_id = " + per_id);
        ResponseDo<BorrowAgreement> agreement = loanService.getBorrowAgreement(per_id);
        request.setAttribute("agreement", agreement);
        if (CodeReturn.success !=agreement.getCode()) {
            return "agreement/phoneAccreditService";
        }
        if (agreement.getData().getTotalTermNum() == 1){
            return "agreement/phoneAccreditService";
        }
        return "agreement/phoneAccredit";
    }

    /**
     * 数字证书
     *
     * @param request
     * @param per_id
     * @return
     */
    @RequestMapping("/numberPage")
    @ApiDoc(Admin.class)
    public String numberPage(HttpServletRequest request, @RequestParam String per_id) {
        logger.info("数字证书请求参数 per_id" + per_id);
        NoteResult result = loanService.getIDNumber(per_id);
        request.setAttribute("data", result);
        return "numberCertificate";
    }

}
