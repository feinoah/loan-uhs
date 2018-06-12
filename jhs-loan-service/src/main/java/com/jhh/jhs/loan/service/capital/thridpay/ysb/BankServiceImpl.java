package com.jhh.jhs.loan.service.capital.thridpay.ysb;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.app.BQSService;
import com.jhh.jhs.loan.api.app.RiskService;
import com.jhh.jhs.loan.api.app.UserService;
import com.jhh.jhs.loan.api.constant.Constants;
import com.jhh.jhs.loan.api.entity.BindCardVo;
import com.jhh.jhs.loan.api.loan.BankService;
import com.jhh.jhs.loan.common.constant.PayCenterChannelConstant;
import com.jhh.jhs.loan.common.constant.QuickPayBindStatusConstant;
import com.jhh.jhs.loan.common.util.CodeReturn;
import com.jhh.jhs.loan.constant.NodeConstant;
import com.jhh.jhs.loan.entity.app.Bank;
import com.jhh.jhs.loan.entity.app.BorrowList;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.entity.manager.BankBan;
import com.jhh.jhs.loan.entity.manager.BankList;
import com.jhh.jhs.loan.mapper.app.BankMapper;
import com.jhh.jhs.loan.mapper.app.BorrowListMapper;
import com.jhh.jhs.loan.mapper.app.PersonMapper;
import com.jhh.jhs.loan.mapper.manager.BankBanMapper;
import com.jhh.jhs.loan.mapper.manager.BankListMapper;
import com.jhh.jhs.loan.mapper.manager.RepaymentPlanMapper;
import com.jhh.jhs.loan.service.capital.CollectUtils;
import com.jhh.pay.driver.pojo.*;
import com.jhh.pay.driver.service.TradeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.JedisCluster;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 绑定银行卡功能实现类
 * 悠悠多多的service
 *
 * @author xuepengfei
 *         2016年11月7日上午9:17:08
 */
@Service
public class BankServiceImpl implements BankService {

    private static final Logger logger = LoggerFactory.getLogger(BankServiceImpl.class);

    @Autowired
    private BankMapper bankMapper;
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private RiskService riskService;
    @Autowired
    private BQSService bqsService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private UserService userService;

    @Autowired
    private RepaymentPlanMapper repaymentPlanMapper;
    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private BankListMapper bankListMapper;

    @Autowired
    private BankBanMapper bankBanMapper;

    @Value("${endDate}")
    private String end;

    /**
     * 1.白骑士  2查询本地及第三方子协议 3绑卡
     */
    @Override
    public NoteResult bindingBank(String per_id, String bankCode, String bank_num, String phone, String status, String tokenKey, String device) {
        NoteResult result = new NoteResult("201", "系统繁忙");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date now = new Date();
        try {
            //根据per_id查询用户信息 : 姓名，身份号
            Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
            String cardNum = person.getCardNum();
            String name = person.getName();
            String personPhone = person.getPhone();// App 手机号

            if (!bindingBQS(per_id, name, cardNum, bank_num, phone, tokenKey, device)) {
                riskService.createBpmNode(per_id, NodeConstant.BANK_NODE_ID, "NS003", "");
                result.setCode("300");
                result.setInfo("白骑士认证建议拒绝");
                return result;
            }
            // 校验手机号与银行预留手机号是否一致
            if (!ObjectUtils.isEmpty(person) && !phone.equals(personPhone)){
                logger.info("手机号与银行预留手机号不一致,修改借款表【borrow_list】状态为BS008");

                riskService.createBpmNode(per_id,NodeConstant.BANK_NODE_ID,CodeReturn.STATUS_BPM_FAIL,"手机号与银行预留手机号不一致,修改借款表状态为BS008");
                BorrowList borrowList = borrowListMapper.selectNow(Integer.parseInt(per_id));

                if(!ObjectUtils.isEmpty(borrowList)) {
                    String borrStatus = borrowList.getBorrStatus();
                    if(CodeReturn.STATUS_APLLY.equals(borrStatus)
                            || CodeReturn.STATUS_WAIT_SIGN.equals(borrStatus)
                            || CodeReturn.STATUS_SIGNED.equals(borrStatus)) {
                        BorrowList bl = new BorrowList();
                        bl.setId(borrowList.getId());
                        bl.setBorrStatus(CodeReturn.STATUS_REVIEW_FAIL);
                        borrowListMapper.updateByPrimaryKeySelective(bl);
                    }
                }
                return new NoteResult(CodeReturn.FAIL_CODE,"手机号要与银行预留手机号一致");
            }

            //查卡
            Bank exist = bankMapper.selectByBankNumAndStatus(bank_num);
            if (exist != null) {
                //表中的银行卡是否是该per_id下的
                if (!per_id.equals(exist.getPerId().toString())) {
                    //不是该per_id下的银行卡，返回绑定失败
                    return new NoteResult("201", "同一张银行卡不可多人绑定");
                }
                if (StringUtils.isEmpty(exist.getSubContractNum())) {
                    String response = CollectUtils.requestBind(bank_num, name, cardNum, phone);
                    JSONObject res = JSONObject.parseObject(response);
                    String code = res.getString("result_code");
                    String msg = res.getString("result_msg");
                    String state = res.getString("status");
                    logger.info("第三方返回结果：" + response);
                    if ("0000".equals(code) && "00".equals(state)) {//第三方受理成功
                        //获取子协议号
                        String subContractId = res.getString("subContractId");
                        exist.setSubContractNum(subContractId);
                        int k = bankMapper.updateByPrimaryKeySelective(exist);
                        NoteResult create = riskService.createBpmNode(per_id, NodeConstant.BANK_NODE_ID, "NS002", "");
                        if (k > 0 && CodeReturn.SUCCESS_CODE.equals(create.getCode())) {
                            // 数据更改 删除缓存
                            result.setCode(CodeReturn.SUCCESS_CODE);
                            result.setData(subContractId);
                            result.setInfo("成功");
                            return result;
                        }
                    } else {//第三方受理失败
                        exist.setResultCode(code);
                        exist.setResultMsg(msg);
                        bankMapper.updateByPrimaryKeySelective(exist);
                        result.setCode(CodeReturn.FAIL_CODE);
                        result.setData(code);
                        result.setInfo(msg);
                        return result;

                    }
                } else {
                    result.setCode(CodeReturn.FAIL_CODE);
                    result.setInfo("该银行卡已经绑定");
                    return result;
                }
            } else {
                //数据库中插入一条银行卡数据
                Bank bank = new Bank();
                bank.setPerId(Integer.valueOf(per_id));
                BankList bankList = bankListMapper.selectByBankCode(bankCode);
                bank.setBankId(String.valueOf(bankList.getId()));
                bank.setBankNum(bank_num);
                bank.setPhone(phone);
                bank.setBankName(bankList.getBankName());
                bank.setStatus("0");
                int i = bankMapper.insertSelective(bank);
                if (i > 0) {//插入数据成功
                    String response = CollectUtils.requestBind(bank_num, name, cardNum, phone);
                    JSONObject res = JSONObject.parseObject(response);
                    String code = res.getString("result_code");
                    String msg = res.getString("result_msg");
                    String state = res.getString("status");
                    logger.info("第三方返回结果：" + response);

                    if ("0000".equals(code) && "00".equals(state)) {//第三方受理成功
                        //获取子协议号
                        String subContractId = res.getString("subContractId");
                        if ("1".equals(status)) {
                            //如果本次绑定是主卡，把用户原来主卡状态改为副卡
                            bankMapper.updateBankStatus("2", Integer.valueOf(per_id), "1");
                            //更新person
                            person.setBankName(bankList.getBankName());
                            person.setBankCard(bank_num);
                            personMapper.updateByPrimaryKeySelective(person);
                        }
                        bank.setStatus(status);
                        bank.setStartDate(now);
                        Date endDate = sdf.parse(end);
                        bank.setEndDate(endDate);
                        bank.setResultCode("0000");
                        bank.setResultMsg(msg);
                        bank.setSubContractNum(subContractId);
                        int k = bankMapper.updateByPrimaryKeySelective(bank);
                        NoteResult create = riskService.createBpmNode(per_id, NodeConstant.BANK_NODE_ID, "NS002", "");
                        if (k > 0 && CodeReturn.SUCCESS_CODE.equals(create.getCode())) {
                            // 数据更改 删除缓存
                            result.setCode(CodeReturn.SUCCESS_CODE);
                            result.setData(subContractId);
                            result.setInfo("成功");
                            return result;
                        }
                    } else {//第三方受理失败
                        bank.setResultCode(code);
                        bank.setResultMsg(msg + res.getString("desc"));
                        bankMapper.updateByPrimaryKeySelective(bank);
                        result.setCode(CodeReturn.FAIL_CODE);
                        result.setData(code);
                        result.setInfo(StringUtils.isEmpty(res.getString("desc")) ? msg : res.getString("desc"));
                        return result;

                    }
                }
            }

            //数据有更改  删除缓存
        } catch (Exception e) {
            logger.info("出错：" + e.getMessage(), e);
            result.setCode("201");
            result.setInfo("系统繁忙");
            return result;
        }
        return result;
    }


    /**
     * 1.支付中心绑卡
     */
    @Override
    @Transactional
    public NoteResult payCenterBindCard(BindCardVo vo) {
        NoteResult result = new NoteResult("201", "系统繁忙");
        try {
            //根据per_id查询用户信息 : 姓名，身份号
            Person person = personMapper.selectByPrimaryKey(Integer.valueOf(vo.getPer_id()));
            if (!bindingBQS(vo.getPer_id(), person.getName(), person.getCardNum(), vo.getBank_num(), vo.getPhone(), vo.getTokenKey(), vo.getDevice())) {
                riskService.createBpmNode(vo.getPer_id(), NodeConstant.BANK_NODE_ID, "NS003", "");
                result.setCode("300");
                result.setInfo("白骑士认证建议拒绝");
                return result;
            }
            //----------------------------绑卡开始---------------------------------------
            //查询银行卡是否存在
            Bank bank = bankMapper.selectByBankNumAndStatus(vo.getBank_num());
            if ((bank != null || !person.getPhone().equals(vo.getPhone())) && !PayCenterChannelConstant.PAY_CHANNEL_HLB_QUICK.equals(vo.getChannel())) {
                return new NoteResult(CodeReturn.REPEAT_BIND,"该银行卡已绑定或手机号不是注册手机");
            } else {
                BankInfo bindingBankInfo = new BankInfo();
                bindingBankInfo.setBankCard(vo.getBank_num());
                QueryResponse<BankBinVo> bankBin = tradeService.getBankBin(bindingBankInfo);
                logger.info("查询用户绑卡银行卡卡bin返回结果 bankBin = \n"+bankBin);
                if (bankBin != null && "SUCCESS".equals(bankBin.getCode())) {
                    Example queryExample = new Example(BankBan.class);
                    Example.Criteria criteria = queryExample.createCriteria();
                    criteria.andEqualTo("bankCode", bankBin.getData().getBankCode());
                    criteria.andIn("type", Arrays.asList("4", "3"));
                    List<BankBan> banBan = bankBanMapper.selectByExample(queryExample);
                    if (banBan != null && banBan.size() > 0) {
                        return NoteResult.FAIL_RESPONSE("当前不支持"+bankBin.getData().getBankName()+"银行卡，请更换其它银行银行卡");
                    }
                } else {
                    logger.error(bankBin == null ? "验证银行卡失败" : bankBin.getMsg());
                    return NoteResult.FAIL_RESPONSE("验证银行卡失败，请稍候再试");
                }

                //组装验卡参数
                BindRequest request = assemblyBindRequest(person, vo);
                BindResponse bindResponse = tradeService.bindCard(request);
                logger.info("用户绑卡支付中心返回参数 BindResponse = " + bindResponse);
                //验证银行卡正确性
                if (verifyBindCard(bindResponse)) {
                    Map<String, Object> bankInfo = bindResponse.getExtension();
                    logger.info("绑卡成功后 用户卡牌信息 bankInfo" +bankInfo);
                    vo.setBankName((String) bankInfo.get("bank_name"));
                    vo.setBankCode((String) bankInfo.get("bank_code"));
                    //绑卡
                    changeNodeAndPersonRedund(vo, person);
                    saveBankCard(vo, bindResponse.getMsg());
                    NoteResult noteResult = riskService.createBpmNode(String.valueOf(person.getId()), NodeConstant.BANK_NODE_ID, "NS002", "");
                    if (CodeReturn.SUCCESS_CODE.equals(noteResult.getCode())){
                        return NoteResult.SUCCESS_RESPONSE();
                    }else {
                        return NoteResult.FAIL_RESPONSE(noteResult.getInfo());
                    }
                } else {
                    return NoteResult.FAIL_RESPONSE(bindResponse.getMsg());
                }

            }
        } catch (Exception e) {
            logger.error("绑卡接口出现异常，请稍候", e);
            return NoteResult.FAIL_RESPONSE(e.getMessage() == null ? "绑卡失败，请稍候再试" : e.getMessage());
        }
    }

    private void changeNodeAndPersonRedund(BindCardVo vo, Person person) {
        if ("1".equals(vo.getStatus())) {
            //如果本次绑定是主卡，把用户原来主卡状态改为副卡
            bankMapper.updateBankStatusWithoutBankNum("2", person.getId(), "1",vo.getBank_num());
            //更新person
            person.setBankName(vo.getBankName());
            person.setBankCard(vo.getBank_num());
            personMapper.updateByPrimaryKeySelective(person);
        }
    }

    /**
     * 验证绑卡
     *
     * @param bindResponse
     */
    private boolean verifyBindCard(BindResponse bindResponse) throws Exception {
        if (bindResponse == null) {
            throw new Exception("支付中心绑卡出现错误");
        }
        if (Constants.PayStyleConstants.JHH_PAY_STATE_SUCCESS.equals(bindResponse.getState())) {
            return true;
        } else {
            return false;
        }
    }

    private void saveBankCard(BindCardVo vo, String msg) throws ParseException {
        // 查询该卡在数据库是否存在
        Bank bank = bankMapper.selectByBankNumAndStatus(vo.getBank_num());
        Bank bank1 = new Bank();
        bank1.setPerId(Integer.parseInt(vo.getPer_id()));
        bank1.setBankNum(vo.getBank_num());
        bank1.setPhone(vo.getPhone());
        bank1.setBankName(vo.getBankName());
        bank1.setBankCode(vo.getBankCode());
        BankList bankList = bankListMapper.selectByBankCode(vo.getBankCode());
        bank1.setBankId(String.valueOf(bankList.getId()));
        bank1.setStatus(vo.getStatus());
        bank1.setStartDate(new Date());
        bank1.setEndDate(new SimpleDateFormat("yyyyMMdd").parse(end));
        bank1.setResultCode("0000");
        bank1.setResultMsg(msg);
        // 判断本次是否为快捷支付绑卡
        if(vo.getValidateCode() != null){
            bank1.setQuickPayStatus(QuickPayBindStatusConstant.QUICK_PAY_HLB);
        }
        if(bank == null){
            bankMapper.insertSelective(bank1);
        }else{
            bank1.setId(bank.getId());
            bankMapper.updateByPrimaryKey(bank1);
        }
    }

    private BindRequest assemblyBindRequest(Person person, BindCardVo vo) {
        BindRequest request = new BindRequest();
        request.setAppId(Constants.PayStyleConstants.YHS_BIND_CARD_APPID);
        request.setPersonName(person.getName());
        request.setBankCard(vo.getBank_num());
        request.setBankMobile(person.getPhone());
        request.setBankName(vo.getBankName());
        request.setIdCardNo(person.getCardNum());
        request.setValidateCode(vo.getValidateCode());
        request.setIsActualBind("true");
        request.setPurpose("绑卡");
        request.setTs(String.valueOf(new Date().getTime()/1000));
        request.setSign("12345");
        request.setExtension(vo.getExtension());
        //TODO 待支付中心驱动包更新需设置渠道
        return request;
    }

    @Override
    public NoteResult queryContractId(String per_id, String bank_num) {
        NoteResult result = new NoteResult("201", "系统繁忙");
        try {
            Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
            String idCardNo = person.getCardNum();
            String name = person.getName();

            //---------------真实请求第三方查询------------------
            String response = CollectUtils.requestQuery(bank_num, name, idCardNo);

            JSONObject res = JSONObject.parseObject(response);
            String result_code = res.getString("result_code");
            String result_msg = res.getString("result_msg");
            String status = res.getString("status");

            //---------------------模拟----------------------------
//            if ("on".equals(isTest)) {
//                result_code = "0000";
//                result_msg = "模拟第三方";
//            }
            //-----------------------------------------------------

            if ("0000".equals(result_code) && "1".equals(status)) {//查询成功
                //---------------------获取子协议号---------------
                String subContractId = res.getString("subContractId");
                //---------------------模拟---------------------
//                if ("on".equals(isTest)) {
//                    subContractId = "111111";
//                }
                //----------------------------------------------
                result.setCode("200");
                result.setData(subContractId);
                result.setInfo(result_msg);

            } else {
                result.setCode(result_code);
                result.setInfo(result_msg);

            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode("201");
            result.setInfo("系统繁忙");
            return result;
        }

        return result;
    }

    @Override
    public boolean verifyBQS(String per_id, String name, String card_num, String bank_num, String bank_phone, String tokenKey, String device) {
        Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
        return bqsService.runBQS(person.getPhone(), person.getName(), person.getCardNum(), "verify", tokenKey, device);
    }

    @Override
    public boolean bindingBQS(String per_id, String name, String card_num, String bank_num, String bank_phone, String tokenKey, String device) {
        Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
        return bqsService.runBQS(person.getPhone(), person.getName(), person.getCardNum(), "verify", tokenKey, device);
    }

    @Override
    public NoteResult personBanks(String per_id) {
        NoteResult result = new NoteResult("201", "系统繁忙");
        try {
            List<Bank> banks = bankMapper.selectAllBanks(per_id);
            result.setCode("200");
            result.setInfo("成功");
            result.setData(banks);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode("201");
            result.setInfo("系统繁忙");
        }

        return result;
    }

    @Override
    public boolean changeBankStatus(String per_id, String bank_num) {

        try {
            Bank bank = bankMapper.selectByBankNumAndStatus(bank_num);
            String oldStatus = bank.getStatus();
            if ("1".equals(oldStatus)) {
                // 如果此银行卡已经是主卡 不允许更改状态
                return false;
            } else {
                // 此银行卡不是主卡， 把原来的主卡 改为副卡
                bankMapper.updateBankStatus("2", Integer.valueOf(per_id), "1");
                // 把此卡转成主卡
                bank.setStatus("1");
                bankMapper.updateByPrimaryKeySelective(bank);
                //更改person表状态
                Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
                person.setBankCard(bank.getBankNum());
                person.setBankName(bank.getBankName());
                personMapper.updateByPrimaryKeySelective(person);
                logger.info("*************用户主动更换主卡成功");
                // 数据更改 删除缓存
                return true;

            }


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取可以代扣及支付的银行卡列表
     *
     * @return
     */
    @Override
    public NoteResult getBankList() {
    //    String redis = jedisCluster.get(RedisConst.BANKLIST_KEY);
        BankBaseInfo[] BankBaseInfo;
       // if (StringUtils.isEmpty(redis)) {
            QueryRequest request = new QueryRequest();
            request.setAppId(Constants.PayStyleConstants.YHS_DEDUCT_APPID);
            QueryResponse<BankBaseInfo[]> bankList = tradeService.getIntersectBanksByAppId(request);
            if (bankList == null) {
                logger.error("银行卡列表获取失败");
                return NoteResult.FAIL_RESPONSE("银行卡列表获取失败");
            } else {
                logger.info("调用支付中心查询银行卡列表接口返回结果 QueryResponse<BankBaseInfo> = " + bankList.toString());
                if (Constants.PayStyleConstants.JHH_PAY_STATE_SUCCESS.equals(bankList.getCode())) {
                    BankBaseInfo = bankList.getData();
                   // jedisCluster.set(RedisConst.BANKLIST_KEY, JSONArray.toJSONString(BankBaseInfo));
                } else {
                    return NoteResult.FAIL_RESPONSE("银行卡列表获取失败");
                }
     //       }

        } /*else {
            // redis里有
            BankBaseInfo = JSONArray.parseObject(redis,BankBaseInfo[].class);
        }*/
        NoteResult noteResult = NoteResult.SUCCESS_RESPONSE();
        noteResult.setData(BankBaseInfo);
        return noteResult;
    }


    private String getBankName(int bankId) {
        String bankName = "";
        List<BankList> bankList = JSONObject.parseArray(JSONObject.toJSONString(getBankList().getData()), BankList.class);
        for (BankList bank : bankList) {
            if (bankId == bank.getId()) {
                bankName = bank.getBankName();
            }
        }
        return bankName;
    }

    /**
     * 扣款前验证是否可以扣款
     *
     * @param borrId
     * @return
     */
    @SuppressWarnings("Duplicates")
    private NoteResult canPayCollect(String borrId, double thisAmount) {

        NoteResult result = new NoteResult(CodeReturn.FAIL_CODE, "失败");
        try {
            // 查出应还金额减去p状态还款订单总额=剩余可还金额
            double canPay = repaymentPlanMapper.selectCanPay(borrId);
            logger.error("本次提交borrId:" + borrId + ",本次提交金额：" + thisAmount + ",剩余应还金额：" + canPay);

            // 如果本次订单金额大于剩余可还金额 不允许还款及代扣
            if (canPay == 0 || thisAmount > canPay) {
                result.setInfo("有正在处理中的还款，当前最多可以还款" + canPay + "元");
                result.setData(canPay);
                return result;
            } else {
                result.setCode(CodeReturn.SUCCESS_CODE);
                result.setData(canPay);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new NoteResult(CodeReturn.FAIL_CODE, "失败");
        }

    }


}
