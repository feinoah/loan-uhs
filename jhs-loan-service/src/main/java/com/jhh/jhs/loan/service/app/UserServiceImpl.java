package com.jhh.jhs.loan.service.app;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.app.BQSService;
import com.jhh.jhs.loan.api.app.RiskService;
import com.jhh.jhs.loan.api.app.UserService;
import com.jhh.jhs.loan.api.constant.StateCode;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.entity.capital.TradeVo;
import com.jhh.jhs.loan.api.notice.UserBehaviorNoticeService;
import com.jhh.jhs.loan.api.sms.SmsService;
import com.jhh.jhs.loan.common.constant.PayCenterChannelConstant;
import com.jhh.jhs.loan.common.enums.SmsTemplateEnum;
import com.jhh.jhs.loan.common.util.*;
import com.jhh.jhs.loan.entity.app.Bank;
import com.jhh.jhs.loan.entity.app.BorrowList;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.constant.Constant;
import com.jhh.jhs.loan.entity.app_vo.MyBorrow;
import com.jhh.jhs.loan.entity.app_vo.PersonInfo;
import com.jhh.jhs.loan.entity.common.Constants;
import com.jhh.jhs.loan.entity.loan.PerAccountLog;
import com.jhh.jhs.loan.entity.manager.*;
import com.jhh.jhs.loan.entity.utils.RepaymentDetails;
import com.jhh.jhs.loan.mapper.app.BankMapper;
import com.jhh.jhs.loan.mapper.app.BankRoutingMapper;
import com.jhh.jhs.loan.mapper.app.BorrowListMapper;
import com.jhh.jhs.loan.mapper.app.CodeValueMapper;
import com.jhh.jhs.loan.mapper.app.PersonMapper;
import com.jhh.jhs.loan.mapper.contract.ContractMapper;
import com.jhh.jhs.loan.mapper.loan.PerAccountLogMapper;
import com.jhh.jhs.loan.mapper.manager.*;
import com.jhh.pay.driver.pojo.BankBinVo;
import com.jhh.pay.driver.pojo.BankInfo;
import com.jhh.pay.driver.pojo.BindRequest;
import com.jhh.pay.driver.pojo.BindResponse;
import com.jhh.pay.driver.pojo.PayRequest;
import com.jhh.pay.driver.pojo.QueryResponse;
import com.jhh.pay.driver.service.TradeService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.JedisCluster;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author xingmin
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final String WHITE_LIST_KEY = Constants.YM_ADMIN_SYSTEN_KEY + Constants.WHITELIST_PERID;

    private static final String WHITE_LIST_PHONE_KEY = Constants.YM_ADMIN_SYSTEN_KEY + Constants.WHITELIST_PHONE;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private FeedbackMapper feedMapper;

    @Autowired
    private QuestionMapper quMapper;

    @Autowired
    private MsgMapper msgMapper;

    @Autowired
    private MsgTemplateMapper msgTemplateMapper;

    @Autowired
    private BorrowListMapper borrowListMapper;

    @Autowired
    private PerAccountLogMapper palMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private BQSService bqsService;

    @Autowired
    private RepaymentPlanMapper repaymentPlanMapper;

    @Autowired
    private ContractMapper contractMapper;

    @Autowired
    private CodeValueMapper codeValueMapper;

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private TradeService tradeService;
    @Autowired
    private RiskService riskService;
    @Autowired
    private UserBehaviorNoticeService userBehaviorNoticeService;

    @Autowired
    private BankMapper bankMapper;

    @Autowired
    private BankRoutingMapper bankRoutingMapper;

    @Override
    public String userLogin(String phone, String password) {
        try {
            Person p = personMapper.getPersonByPhone(phone);
            if (p == null) {
                JSONObject data = new JSONObject();
                data.put("per_id", "");
                data.put("token", "");
                return getReturnResult(CodeReturn.fail, "该手机号未注册", data);
            }

            Person user = new Person();
            user.setPhone(phone);
            user.setPassword(password);
            List<Person> personList = personMapper.userLogin(user);
            if (personList == null || personList.size() < 1) {
                JSONObject data = new JSONObject();
                data.put("per_id", "");
                data.put("token", "");
                return getReturnResult(CodeReturn.fail, "账号密码不匹配", data);
            }

            if (personList.size() > 1) {
                JSONObject data = new JSONObject();
                data.put("per_id", "");
                data.put("token", "");
                return getReturnResult(CodeReturn.fail, "该手机号有多个账户", data);
            }

            String token = UUID.randomUUID().toString();
            Person pp = new Person();
            pp.setId(personList.get(0).getId());
            pp.setTokenId(token);
            pp.setLoginTime(Calendar.getInstance().getTime());
            personMapper.updateByPrimaryKeySelective(pp);

            //删除用户登录缓存
            userBehaviorNoticeService.delLoginRedis(phone);

            JSONObject data = new JSONObject();
            data.put("per_id", personList.get(0).getId());
            data.put("token", token);
            return getReturnResult(CodeReturn.success, "登录成功", data);
        } catch (Exception e) {
            logger.error("系统错误");
            e.printStackTrace();
            JSONObject data = new JSONObject();
            data.put("per_id", "");
            data.put("token", "");
            return getReturnResult(CodeReturn.success, "系统错误", data);
        }
    }

    @Override
    public String userLoginByAuthCode(String phone, String authCode){
        try {
            Person p = personMapper.getPersonByPhone(phone);
            // 用户未注册，自动注册
            if (p == null) {
                Random random = new Random();
                String radomInt = "";
                for (int i = 0; i < 6; i++) {
                    radomInt += random.nextInt(10);
                }
                String password = radomInt.toString();
                //注册方法
                password = MD5Util.encodeToMd5(password);
                Person user = new Person();
                user.setPhone(phone);
                user.setPassword(password);
                user.setIsLogin(1);
                user.setSource("1");
                user.setCreateDate(new Date());
                //注册方法
                int successInt = personMapper.insertSelective(user);
                if (successInt < 1) {
                    return getReturnResult(CodeReturn.PHONE_EXIST, "注册失败，系统错误!", p.getSource());
                }

                //注册成功后，进入登录方法
                String loginSuccess = userService.userLogin(phone, password);
                JSONObject loginObject = JSONObject.parseObject(loginSuccess);
                if (loginObject.getInteger("code") != CodeReturn.success) {
                    return loginSuccess;
                }

                ResponseDo rspDo = smsService.sendSms(SmsTemplateEnum.INIT_PASSWORD_REMIND.getCode(), phone, radomInt);
                if (StateCode.SUCCESS_CODE != rspDo.getCode()) {
                    logger.info("初始密码发送失败！");
                }else {
                    logger.info("初始密码发送成功！");
                }
                return loginSuccess;
            }

            //根据验证码判断
            String code = jedisCluster.get(phone+"_"+authCode);
            if (StringUtils.isEmpty(code)) {
                JSONObject data = new JSONObject();
                data.put("per_id", "");
                data.put("token", "");
                return getReturnResult(CodeReturn.fail, "验证码输入错误", data);
            }

            List<Person> personList = personMapper.userLoginByPhone(phone);
            if (personList.size() > 1) {
                JSONObject data = new JSONObject();
                data.put("per_id", "");
                data.put("token", "");
                return getReturnResult(CodeReturn.fail, "该手机号有多个账户", data);
            }

            String token = UUID.randomUUID().toString();
            Person pp = new Person();
            pp.setId(personList.get(0).getId());
            pp.setTokenId(token);
            pp.setLoginTime(Calendar.getInstance().getTime());
            personMapper.updateByPrimaryKeySelective(pp);

            //删除用户登录缓存
            userBehaviorNoticeService.delLoginRedis(phone);

            JSONObject data = new JSONObject();
            data.put("per_id", personList.get(0).getId());
            data.put("token", token);
            return getReturnResult(CodeReturn.success, "登录成功", data);
        } catch (Exception e) {
            logger.error("系统错误");
            e.printStackTrace();
            JSONObject data = new JSONObject();
            data.put("per_id", "");
            data.put("token", "");
            return getReturnResult(CodeReturn.fail, "系统错误", data);
        }
    }

    @Override
    public String checkInviteCode(String inviteCode) {
        if (!RegexUtil.isNumber(inviteCode)) {
            return getReturnResult(CodeReturn.fail, "邀请码无效,请重新填写!", inviteCode);
        }
        Person person;
        try {
            person = personMapper.selectByPrimaryKey(Integer.parseInt(inviteCode));
        } catch (Exception e) {
            person = null;
            logger.info(String.format("----> checkInviteCode【%s】 has exception: %s", inviteCode, e));
        }
        if (person == null) {
            return getReturnResult(CodeReturn.fail, "邀请码无效,请重新填写!", inviteCode);
        }
        return getReturnResult(CodeReturn.success, "邀请码有效!", inviteCode);
    }

    @Override
    public String userRegister(Person user) {
        try {
            // 幂等性操作 防止重复注册
            if (!StringUtils.isEmpty(jedisCluster.get(RedisConst.REGISTER_KEY + user.getPhone()))) {
                return alreadyRegistered(user.getPhone());
            }

            String setnx = jedisCluster.set(RedisConst.REGISTER_KEY + user.getPhone(), user.getPhone(), "NX", "EX", 60 * 60 * 24);
            if (!"OK".equals(setnx)) {
                return alreadyRegistered(user.getPhone());
            }

            Person p = personMapper.getPersonByPhone(user.getPhone());
            if (p != null) {
                return getReturnResult(CodeReturn.PHONE_EXIST, "注册失败，该手机号已经注册!", p.getSource());
            }

            personMapper.insertSelective(user);
            //用户注册成功，并且为H5渠道，添加注册缓存。2018年5月29日 20:49:26
            if(Detect.notEmpty(user.getSource()) && !user.getSource().equals("1") ){
                userBehaviorNoticeService.addRegisterRedis(user.getPhone());
            }
            return getReturnResult(CodeReturn.success, "注册成功!", user.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return getReturnResult(CodeReturn.fail, "系统错误!", "");
        }
    }

    @Override
    public String updatePassword(Person user) {
        try {
            int li = personMapper.updatePassword(user);
            if (li > 0) {
                return getReturnResult(CodeReturn.success, "密码修改成功!", "");
            } else {
                return getReturnResult(CodeReturn.fail, "密码修改失败!", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getReturnResult(CodeReturn.fail, "系统错误!", "");
        }
    }

    @Override
    public String getPersonInfo(String userId) {
        try {
            PersonInfo p = personMapper.getPersonInfo(userId);
            return getReturnResult(CodeReturn.success, "查询成功!", JSON.toJSON(p));
        } catch (Exception e) {
            e.printStackTrace();
            return getReturnResult(CodeReturn.fail, "系统错误!", "");
        }
    }

    @Override
    public String personUpdatePassword(Person user) {
        try {
            Person user1 = personMapper.selectByPrimaryKey(user.getId());
            if (user.getOldPassword().equals(user1.getPassword())) {
                int a = personMapper.personUpdatePassword(user);
                return getReturnResult(CodeReturn.success, "密码修改成功!", a);
            } else {
                return getReturnResult(CodeReturn.fail, "原密码不正确!", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getReturnResult(CodeReturn.fail, "系统错误!", "");
        }
    }

    @Override
    public String userFeedBack(Feedback feed) {
        try {
            feedMapper.insertSelective(feed);
            return getReturnResult(CodeReturn.success, "反馈成功!", "");
        } catch (Exception e) {
            e.printStackTrace();
            return getReturnResult(CodeReturn.fail, "系统错误!", "");
        }
    }

    @Override
    public String getQuestion() {
        try {
            List<Question> quList;

            String redis = jedisCluster.get(RedisConst.QUESTION_KEY);
            if (StringUtils.isEmpty(redis)) {
                // redis里没有
                quList = quMapper.selectAllQuestion();
                // redis set
                jedisCluster.set(RedisConst.QUESTION_KEY, com.alibaba.fastjson.JSONObject.toJSONString(quList));

            } else {
                // redis里有
                quList = (List<Question>) com.alibaba.fastjson.JSONObject.parse(redis);
            }

            return getReturnResult(CodeReturn.success, "查询成功!", JSON.toJSON(quList));
        } catch (Exception e) {
            e.printStackTrace();
            return getReturnResult(CodeReturn.fail, "系统错误!", "");
        }
    }

    @Override
    public String getMessageByUserId(String userId, int nowPage, int pageSize) {
        try {
            int start = (nowPage - 1) * pageSize;
            List<Msg> msgList = msgMapper.getMessageByUserId(userId, start, pageSize);
            JSONArray arr = (JSONArray) JSON.toJSON(msgList);
            for (int i = 0; i < msgList.size(); i++) {
                if (null == msgList.get(i).getCreate_time()
                        || "".equals(msgList.get(i).getCreate_time())) {
                    arr.getJSONObject(i).put("create_time", "");
                } else {
                    arr.getJSONObject(i).put(
                            "create_time",
                            DateUtil.getDateStringToHHmmss(DateUtil
                                    .getDateHHmmss(msgList.get(i)
                                            .getCreate_time())));
                }
            }
            return getReturnResult(CodeReturn.success, "查询成功!", arr);
        } catch (Exception e) {
            logger.error("查询失败");
            return getReturnResult(CodeReturn.fail, "查询失败!", "");
        }
    }

    @Override
    public String setMessage(String userId, String templateId, String params) {
        JSONObject obj = new JSONObject();
        logger.info("发送站内信参数:userId:"+userId+",templateId:"+templateId+",params:"+params+"");
        try {
            String[] cc = Arrays.stream(params.split(",")).map(t -> isNumeric(t) ? String.format("%.2f", Double.valueOf(t)) : t).toArray(String[]::new);
            // 定义最后的消息内容
            String dd = "";
            // 获取消息模版
            MsgTemplate msgTemplate = msgTemplateMapper
                    .selectByPrimaryKey(Integer.parseInt(templateId));
            if (null != msgTemplate) {

                if ("1".equals(msgTemplate.getStatus())) {
                    // 获取模版的内容
                    String ll = msgTemplate.getContent();
                    // 获取模版的标题
                    String title = msgTemplate.getTitle();
                    // 分割模版内容
                    String[] aa = ll.split("\\{");
                    // 将模版内容和参数拼接成最后的消息内容
                    for (int i = 0; i < aa.length; i++) {
                        String[] bb = aa[i].split("}");
                        if (bb.length > 1) {
                            dd += cc[i - 1] + bb[1];
                        } else {
                            dd += aa[i];
                        }
                    }
                    Msg msg = new Msg();
                    msg.setContent(dd);
                    msg.setTitle(title);
                    msg.setPerId(Integer.parseInt(userId));
                    msg.setStatus("n");
                    msg.setType(1);
                    msg.setCreateTime(new Date());
                    msgMapper.insertSelective(msg);
                    obj.put("code", CodeReturn.success);
                    obj.put("info", "消息发送成功");
                    obj.put("data", dd);
                    logger.info("站内信发送成功！userId:"+userId+"");
                } else {
                    obj.put("code", CodeReturn.fail);
                    obj.put("info", "模版已失效");
                    logger.info("站内信模版已失效！");
                }
            } else {
                obj.put("code", CodeReturn.fail);
                obj.put("info", "模版不存在");
                logger.info("站内信模版不存在！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            obj.put("code", CodeReturn.fail);
            obj.put("info", "消息发送失败,请检查参数和模版是否匹配！");
            logger.info("站内信发送失败,请检查参数和模版是否匹配！");
        }
        return obj.toString();
    }

    @Override
    public String getPersonByPhone(String phone) {
        JSONObject obj = new JSONObject();

        try {
            Person p = personMapper.getPersonByPhone(phone);
            if (null == p) {
                obj.put("code", CodeReturn.success);
                obj.put("info", "手机号不存在");

            } else {
                obj.put("code", CodeReturn.fail);
                obj.put("info", "手机号已存在");

            }
        } catch (Exception e) {
            obj.put("code", CodeReturn.systemerror);
            obj.put("info", "系统错误");

        }
        return obj.toString();
    }

    @Override
    public NoteResult getWithdrawInformation(int userId) {
        NoteResult result = new NoteResult(CodeReturn.FAIL_CODE, "失败");

        try {
            com.alibaba.fastjson.JSONObject dataObject = new com.alibaba.fastjson.JSONObject();
            dataObject.put("isAvailable", false);

            result.setCode(CodeReturn.SUCCESS_CODE);
            result.setInfo("成功");
            result.setData(dataObject);
        } catch (Throwable e) {
            logger.error("检测是否可以引导用户下载金狐贷失败", e);
        }

        return result;
    }

    @Override
    public boolean isInWhiteList(String userid) {
        if(Detect.notEmpty(jedisCluster.hget(WHITE_LIST_KEY, userid)) &&
                jedisCluster.hget(WHITE_LIST_KEY, userid).equals("1")){
            return true;
        }
        return false;
    }

    @Override
    public void syncWhiteList() {
        List<Integer> perIds = borrowListMapper.syncWhiteList();
        if(perIds != null){
            //先删除，在保存
            jedisCluster.del(WHITE_LIST_KEY);

            for(Integer perId : perIds){
                if(Detect.isPositive(perId)){
                    jedisCluster.hset(WHITE_LIST_KEY, perId.toString(), "1");
                }
            }
        }
    }

    @Override
    public void syncPhoneWhiteList() {
        List<String> phones = borrowListMapper.syncPhoneWhiteList();
        if(phones != null){
            //先删除，在保存
            jedisCluster.del(WHITE_LIST_PHONE_KEY);

            for(String phone : phones){
                if(phone != null){
                    jedisCluster.hset(WHITE_LIST_PHONE_KEY, phone, "1");
                }
            }
        }
    }

    @Override
    public String getMyBorrowList(String userId, int nowPage, int pageSize) {
        JSONObject obj = new JSONObject();
        try {
            int start = (nowPage - 1) * pageSize;
            List<MyBorrow> borrowLists = borrowListMapper.getMyBorrowList(userId, start, pageSize);
            JSONArray array = (JSONArray) JSONObject.toJSON(borrowLists);
            obj.put("code", CodeReturn.success);
            obj.put("info", "查询成功");
            obj.put("data", array);
        } catch (Exception e) {
            e.printStackTrace();
            obj.put("code", CodeReturn.fail);
            obj.put("info", "查询失败");
            obj.put("data", new JSONArray());
        }

        return obj.toString();
    }

    @Override
    public NoteResult getProdModeByBorrId(String borrId) {
        NoteResult result = new NoteResult(CodeReturn.FAIL_CODE,"失败");
        try {
            List<RepaymentPlan> repaymentTermPlans = repaymentPlanMapper.selectByBorrId(borrId);
            BorrowList borrowList = borrowListMapper.selectByPrimaryKey(Integer.valueOf(borrId));
            String contractUrl = contractMapper.getContractUrl(borrId);
            logger.info(String.format("---->合同图片地址【%s】", contractUrl));
            JSONObject data = new JSONObject();
            data.put("borrId", borrId);
            data.put("borrStatus",borrowList.getBorrStatus());
            data.put("surplusAmount", String.format("%.2f",borrowList.getSurplusAmount()));
            data.put("termNum", borrowList.getTotalTermNum());
            if (borrowList.getNoDepositRefund() != null && borrowList.getNoDepositRefund() ==1){
                //不退押金
                data.put("depositAmount", 0);
            }else{
                //退押金  加负号
                data.put("depositAmount", borrowList.getDepositAmount());
            }

            // 一期不展示提前结算的按钮
            if(1 == borrowList.getTotalTermNum()){
                data.put("earlySettlement",0);
            } else {
                data.put("earlySettlement",1);
            }
            data.put("ransomAmount", borrowList.getRansomAmount());
            data.put("contractUrl", contractUrl);
            data.put("overdueDays", borrowList.getOverdueDays());
            data.put("planList", JSONObject.toJSON(handleRepaymentPlans(repaymentTermPlans, borrowList)));
            result.setData(data);
            result.setCode(CodeReturn.SUCCESS_CODE);
            result.setInfo("成功");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.setInfo("查询失败");
            return result;
        }

    }

    @Override
    public String updateMessageStatus(String userId, String messageId) {
        JSONObject obj = new JSONObject();
        try {
            int i = msgMapper.updateMessageStatus(messageId);
            if (i > 0) {
                obj.put("code", CodeReturn.success);
                obj.put("info", "修改成功");

            } else {
                obj.put("code", CodeReturn.fail);
                obj.put("info", "修改失败");

            }
        } catch (Exception e) {
            e.printStackTrace();
            obj.put("code", CodeReturn.fail);
            obj.put("info", "修改失败,系统错误");

        }
        return obj.toString();
    }

    @Override
    public ResponseDo<RepaymentDetails> getRepaymentDetails(String userId, String borrId,String type) {

        JSONObject obj = new JSONObject();
        try {
            RepaymentDetails repaymentDetails = borrowListMapper.getRepaymentDetails(borrId);
            //默认为查当期应还  如果type == 1 查全部应还
            if ("1".equals(type)){
                repaymentDetails.setPlan_repay(borrowListMapper.getTotalLeft(borrId));
            }

            repaymentDetails.setPer_id(userId);
            // 手续费
            String fee = codeValueMapper.getMeaningByTypeCode("payment_fee", "1");
            String minimum = codeValueMapper.getMeaningByTypeCode("payment_fee", "4");
            repaymentDetails.setCounterFee(fee);
            String actRepay = String.format("%.2f", Double.valueOf(repaymentDetails.getPlan_repay()) + Double.valueOf(fee));
            repaymentDetails.setAct_repay_amount(actRepay);
            repaymentDetails.setMinimum(minimum);
            repaymentDetails.setAlsoAmount(repaymentDetails.getPlan_repay());

           return ResponseDo.newSuccessDo(repaymentDetails);
        } catch (Exception e) {
            e.printStackTrace();
           return ResponseDo.newFailedDo("查询失败");

        }
    }

    @Override
    public String perAccountLog(String userId, int nowPage, int pageSize) {
        JSONObject obj = new JSONObject();
        try {
            int start = (nowPage - 1) * pageSize;
            List<PerAccountLog> pal = palMapper.getPerAccountLog(userId, start,
                    pageSize);
            JSONArray arr = new JSONArray();
            if (null == pal || 0 == pal.size()) {
                obj.put("code", CodeReturn.success);
                obj.put("info", "查询成功");
                obj.put("data", arr);
            } else {
                for (int i = 0; i < pal.size(); i++) {
                    JSONObject obj1 = new JSONObject();
                    obj1.put("id", pal.get(i).getId());
                    obj1.put("perId", pal.get(i).getPerId());
                    obj1.put("operationTypeName", pal.get(i)
                            .getOperationTypeName());
                    obj1.put("operationType", pal.get(i).getOperationType());
                    // 取小数点后两位
                    DecimalFormat df = new DecimalFormat("######0.00");
                    obj1.put("amount", df.format(Double.parseDouble(pal.get(i)
                            .getAmount())));
                    obj1.put("addDateTime", DateUtil
                            .getDateStringToHHmmss(DateUtil.getDateHHmmss(pal
                                    .get(i).getAddDateTime())));
                    arr.add(obj1);
                }
            }
            obj.put("code", CodeReturn.success);
            obj.put("info", "查询成功");
            obj.put("data", arr);
        } catch (Exception e) {
            e.printStackTrace();
            obj.put("code", CodeReturn.fail);
            obj.put("info", "查询失败");

        }
        return obj.toString();
    }

    @Override
    public String canOfOrder(String orderId, String token, String info) {
        JSONObject obj = new JSONObject();
        try {
            Order order = orderMapper.selectBySerial(orderId);
            Order order1 = orderMapper.selectByPid(order.getId());
            Person person = new Person();
            person = personMapper.selectByPrimaryKey(order.getPerId());
            logger.info("orderId==" + orderId + ",token==" + token + ",info==" + info);
            if (token.equals(person.getTokenId())) {
                order.setRlState("f");
                order.setRlRemark(info);
                order1.setRlState("f");
                order1.setRlRemark(info);
                orderMapper.updateByPrimaryKeySelective(order);
                orderMapper.updateByPrimaryKeySelective(order1);
                obj.put("code", CodeReturn.success);
                obj.put("info", "订单取消成功");

            } else {
                obj.put("code", CodeReturn.TOKEN_WRONG);
                obj.put("info", "该帐号已在别的设备登录，请重新登录");

            }
        } catch (Exception e) {
            e.printStackTrace();
            obj.put("code", CodeReturn.fail);
            obj.put("info", "订单取消失败");

        }
        return obj.toString();
    }

    @Override
    public boolean updatePasswordCanbaiqishi(String tokenKey, String event, String userId,String device) {
        Person person = personMapper.selectByPrimaryKey(Integer.valueOf(userId));
        String phone = person.getPhone();
        String name = person.getName() == null ? "" : person.getName();
        String idCardNum = person.getCardNum() == null ? "" : person.getCardNum();
        return bqsService.runBQS(phone, name, idCardNum, event,tokenKey,device);
    }


    @Override
    public int yanzhengtoken(String userId, String token) {
        Person pp = personMapper.selectByPrimaryKey(Integer.parseInt(userId));
        if (token.equals(pp.getTokenId())) {
            return 200;
        } else {
            return 201;
        }
    }

    /**
     * 检查该手机号是否在mysql黑名单
     * @param phone
     * @return
     */
    @Override
    public NoteResult checkBlack(String phone) {
        NoteResult result = new NoteResult();
        try {
            int i = personMapper.checkBlack(phone);
            if (i > 0) {
                result.setCode(CodeReturn.NOW_BORROW_CODE);
                result.setInfo("黑名单");
            } else {
                result.setCode(CodeReturn.SUCCESS_CODE);
                result.setInfo("非黑名单");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new NoteResult(CodeReturn.FAIL_CODE, "查询失败");
        }
        return result;

    }

    @Override
    public String deleteRedis(String per_id) {
        return jedisCluster.del(RedisConst.NODE_KEY + per_id).toString();
    }

    /**
     * 手机号已经被注册过时的返回结果
     * @param phone 注册手机号
     * @return String
     */
    private String alreadyRegistered(String phone) {
        JSONObject obj = new JSONObject();
        logger.error("直接返回，注册重复数据phone" + phone);
        Person p = personMapper.getPersonByPhone(phone);
        obj.put("code", CodeReturn.PHONE_EXIST);
        obj.put("info", "不可重复注册！");
        if (p !=null){
            obj.put("data", p.getSource());
        }
        return obj.toString();
    }

    /**
     * 利用正则表达式判断字符串是否是数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String setRedisData(String key, int time, String data) {
        return jedisCluster.setex(key,time,data);
    }

    @Override
    public String queryRedisData(String key) {
        return jedisCluster.get(key);
    }

    @Override
    public NoteResult checkBankPhone(String per_id, String phone) {
        logger.info("开始校验客户"+ per_id +  "申请银行预留手机号 phone = " + phone);
        NoteResult result = new NoteResult();
        result.setData("");
        if(StringUtils.isEmpty(per_id)){
            result.setCode(CodeReturn.FAIL_CODE);
            result.setInfo("用户不存在");
            return result;
        }
        if(StringUtils.isEmpty(phone)){
            result.setCode(CodeReturn.FAIL_CODE);
            result.setInfo("银行预留手机号不能为空");
            return result;
        }

        Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));

        if(ObjectUtils.isEmpty(person)){
            result.setCode(CodeReturn.FAIL_CODE);
            result.setInfo("用户不存在");
            return result;
        }

        if(phone.equals(person.getPhone())){
            result.setCode(CodeReturn.SUCCESS_CODE);
            result.setInfo("成功");
            return result;
        } else {
            result.setCode(CodeReturn.FAIL_CODE);
            result.setInfo("银行预留手机号不匹配");
            return result;
        }
    }

    @Override
    public NoteResult checkRegisterReviewers(String phone) {
        // 调用风控借口
        return null;
    }

    private String getReturnResult(int code, String info, Object data) {
        JSONObject object = new JSONObject();
        object.put("code", code);
        object.put("info", info);
        object.put("data", data);
        return object.toString();
    }

    @Override
    public NoteResult sendHelipayMsg(String phone, String bankNum) {
        Person person = personMapper.getPersonByPhone(phone);
        //调用合利宝短信接口获取验证码
        PayRequest payRequest = new PayRequest();
        payRequest.setAppId(com.jhh.jhs.loan.api.constant.Constants.PayStyleConstants.YHS_SEND_MSG_APPID);

        BankInfo bankInfo = new BankInfo();
        bankInfo.setBankCard(bankNum);
        bankInfo.setBankMobile(phone);
        payRequest.setBankInfo(bankInfo);

        Map<String, Object> channels = new HashMap<>();
        channels.put(PayCenterChannelConstant.PAY_CHANNEL_HLB_QUICK,new JSONObject());
        payRequest.setChannels(channels);

        Map<String, Object> extension = new HashMap<>();
        extension.put("user_id",person.getId());
        payRequest.setExtension(extension);
        Long orderNum = jedisCluster.incr(RedisConst.HELI_PAY_MSG_ORDER_NUM);

        String date = DateFormatUtils.format(new Date(), "yyyyMMdd");

        payRequest.setOrderNo(new StringBuilder("JHS").append(date).append(orderNum.toString()).toString());
        logger.info("开始发送合利宝绑卡短信, 参数为 -> "+payRequest);
        QueryResponse<BankBinVo> queryResponse = tradeService.sendMsg(payRequest);

        NoteResult noteResult;
        if("200".equals(queryResponse.getCode())){
            noteResult = NoteResult.SUCCESS_RESPONSE();
            //在redis中记录合利宝快捷支付验证码标记
            String valiCodeKey = new StringBuilder(RedisConst.VALIDATE_CODE).append(RedisConst.SEPARATOR).append(phone).toString();
            jedisCluster.setex(valiCodeKey,2*60, PayCenterChannelConstant.PAY_CHANNEL_HLB_QUICK);
        }else{
            noteResult = NoteResult.FAIL_RESPONSE(queryResponse.getMsg());
        }
        return noteResult;
    }

    /**
     * 处理还款计划
     * @param repaymentPlans
     * @param borrowList
     * @return
     */
    private List<RepaymentPlan> handleRepaymentPlans(List<RepaymentPlan> repaymentPlans, BorrowList borrowList) {
        repaymentPlans.forEach(repaymentPlan -> {
            if (repaymentPlan.getIsLast() == null) {
                return;
            }
            if (repaymentPlan.getIsLast() != 1) {
                repaymentPlan.setSurplusAmount(repaymentPlan.getSurplusRentalAmount().add(repaymentPlan.getSurplusPenalty()));
                repaymentPlan.setPaidAmount(repaymentPlan.getRentalAmount().add(repaymentPlan.getPenalty()).subtract(repaymentPlan.getSurplusAmount()));
                return;
            }

            // 不退押金, 押金置为0
            if (borrowList.getNoDepositRefund() != null && borrowList.getNoDepositRefund() == 1) {
                borrowList.setDepositAmount(0F);
            }

            // 结清状态, 押金置为0
            if (StringUtils.equals(Constant.STATUS_PAY_BACK, borrowList.getBorrStatus()) || StringUtils.equals(Constant.STATUS_DELAY_PAYBACK, borrowList.getBorrStatus()) || StringUtils.equals(Constant.STATUS_EARLY_PAYBACK, borrowList.getBorrStatus())) {
                borrowList.setDepositAmount(0F);
            }

            repaymentPlan.setSurplusAmount(repaymentPlan.getSurplusRentalAmount().add(repaymentPlan.getSurplusPenalty()).add(new BigDecimal(borrowList.getSurplusRansomAmount())).subtract(new BigDecimal(borrowList.getDepositAmount())));
            repaymentPlan.setPaidAmount(repaymentPlan.getRentalAmount().add(repaymentPlan.getPenalty()).add(new BigDecimal(borrowList.getRansomAmount())).subtract(repaymentPlan.getSurplusAmount()).subtract(new BigDecimal(borrowList.getDepositAmount())));
        });

        return repaymentPlans;
    }


    /**
     * 查询主动还款默认渠道信息
     *
     * @return
     */
    public CodeValue selectDefaultRepayChannel() {
        return codeValueMapper.selectByCodeType(Constant.REPAY_SWITCH);
    }

    /**
     * 查询合利宝快捷支付绑卡
     * @param bindRequest
     * @return
     */
    @Override
    public BindResponse queryBind(BindRequest bindRequest) {
        return tradeService.queryBind(bindRequest);
    }

    /**
     * 查询快捷支付绑定状态并发送验证码
     * @param phone
     * @param bankNum
     * @return
     */
    @Override
    public NoteResult queryBindAndSendMsg(String phone, String bankNum) {
        Bank bank = bankMapper.selectByBankNumAndStatus(bankNum);
        NoteResult noteResult = new NoteResult();
        noteResult.setCode("666");
        if(bank ==null || bank.getQuickPayStatus() == null){
            // 发送合利宝快捷支付绑定短信 TODO 拉卡拉对接后需判断绑定状态并对发送的验证码做渠道标记
            noteResult = sendHelipayMsg(phone,bankNum);
        }
        return noteResult;
    }

    @Override
    public Integer queryQuickBind(String bankNum) {
        Bank bank = bankMapper.selectByBankNumAndStatus(bankNum);
        return bank == null? null:bank.getQuickPayStatus();
    }


    /**
     * 选择路由渠道
     *
     * @param vo
     */
    @SuppressWarnings("unchecked")
    public TradeVo chooseBankRouting(TradeVo vo) {
        if (com.alibaba.dubbo.common.utils.StringUtils.isNotEmpty(vo.getBankNum()) && !"pay-zfb".equals(vo.getPayChannel())) {
            BankInfo bindingBankInfo = new BankInfo();
            bindingBankInfo.setBankCard(vo.getBankNum());
            try {
                QueryResponse<BankBinVo> bankBin = tradeService.getBankBin(bindingBankInfo);

                logger.info("查询用户绑卡银行卡卡bin返回结果 bankBin = \n" + bankBin);
                if (bankBin != null && "SUCCESS".equals(bankBin.getCode())) {
                    Example queryExample = new Example(BankRouting.class);
                    Example.Criteria criteria = queryExample.createCriteria();
                    criteria.andEqualTo("bankCode", bankBin.getData().getBankCode());
                    criteria.andEqualTo("status", "1");
                    List<BankRouting> bankRouting = bankRoutingMapper.selectByExample(queryExample);
                    if (bankRouting != null && bankRouting.size() == 1) {
                        vo.setPayChannel(bankRouting.get(0).getChannels());
                    }
                } else {
                    logger.error(bankBin == null ? "验证银行卡失败" : bankBin.getMsg());
                }
            } catch (Exception e) {
                logger.error("代扣查询卡bin失败", e);
            }
        }
        return vo;
    }
}