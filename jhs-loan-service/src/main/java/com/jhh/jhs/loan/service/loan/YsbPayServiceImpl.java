package com.jhh.jhs.loan.service.loan;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.app.UserService;
import com.jhh.jhs.loan.api.constant.StateCode;
import com.jhh.jhs.loan.api.contract.ElectronicContractService;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.loan.YsbpayService;
import com.jhh.jhs.loan.api.sms.SmsService;
import com.jhh.jhs.loan.common.enums.SmsTemplateEnum;
import com.jhh.jhs.loan.common.payment.CollectUtil;
import com.jhh.jhs.loan.common.payment.PaymentUtil;
import com.jhh.jhs.loan.common.util.*;
import com.jhh.jhs.loan.entity.app.Bank;
import com.jhh.jhs.loan.entity.app.BorrowList;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.entity.loan.PerAccountLog;
import com.jhh.jhs.loan.entity.manager.Order;
import com.jhh.jhs.loan.entity.utils.BorrPerInfo;
import com.jhh.jhs.loan.entity.utils.RepaymentDetails;
import com.jhh.jhs.loan.mapper.app.BankMapper;
import com.jhh.jhs.loan.mapper.app.BorrowListMapper;
import com.jhh.jhs.loan.mapper.app.PersonMapper;
import com.jhh.jhs.loan.mapper.loan.PerAccountLogMapper;
import com.jhh.jhs.loan.mapper.manager.CollectorsListMapper;
import com.jhh.jhs.loan.mapper.manager.OrderMapper;
import com.jhh.jhs.loan.mapper.manager.RepaymentPlanMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisCluster;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

//import com.jinhuhang.market.api.manage.ManageInfoService;
//import com.jinhuhang.market.model.LoanUserMsg;
//import com.jinhuhang.market.api.manage.ManageInfoService;
//import com.jinhuhang.market.model.LoanUserMsg;


/**
 * @author xuepengfei
 */
@Slf4j
@Service
public class YsbPayServiceImpl implements YsbpayService {

    private static final Logger logger = LoggerFactory.getLogger(YsbPayServiceImpl.class);

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CollectorsListMapper collectorsListMapper;

    @Autowired
    private BorrowListMapper borrMapper;

    @Autowired
    private PerAccountLogMapper palMapper;

    @Autowired
    private RepaymentPlanMapper rpMapper;

    @Autowired
    private UserService userService;

    @Autowired
    SmsService smsService;

    // @Autowired
    // private PerCouponMapper percouMapper;


    @Autowired
    private BankMapper bankMapper;


//    @Autowired
//    private RMISettlementService rMISettlementService;
    @Autowired
    private JedisCluster jedisCluster;
    //    @Autowired
//    private ManageInfoService manageInfoService;
    @Autowired
    ElectronicContractService electronicContractService;
//    @Autowired
//    private DelayQueueService delayQueueService;

    private String isTest = PropertiesReaderUtil.read("third", "isTest");



    // 2017.3.27更改认证支付
    @SuppressWarnings("deprecation")
    @Override
    public String ysbPayment(String userId, String amount, String contract_id, String bank_id,
                             String cardNo, String token) {
        JSONObject obj = new JSONObject();

        //判断金额合法性  小于或等于0  直接拒绝
        if (Double.parseDouble(amount) <= 0) {
            obj.put("code", CodeReturn.fail);
            obj.put("info", "金额不合法！");
            return obj.toString();
        }

        //清结算锁
        String settleLock = jedisCluster.get(RedisConst.SETTLE_LOCK_KEY);
        if (!com.alibaba.dubbo.common.utils.StringUtils.isEmpty(settleLock) && "off".equals(settleLock)) {
            obj.put("code", CodeReturn.fail);
            obj.put("info", "凌晨0点-5点开始为系统维护时间，期间不能进行还款，给您带来的不便敬请谅解！");
            return obj.toString();
        }

        String fee = "2.00";

        //  用户还款金额减去手续费不能超过应还金额
        NoteResult canPay = canPayCollect(contract_id, (Double.parseDouble(amount) - Double.parseDouble(fee)));
        if (CodeReturn.FAIL_CODE.equals(canPay.getCode())) {
            obj.put("code", CodeReturn.fail);
            obj.put("info", canPay.getInfo());
            return obj.toString();
        }


        Person pp = personMapper.selectByPrimaryKey(Integer.parseInt(userId));
        if (token.equals(pp.getTokenId())) {
            logger.info("进入----YsbPayServiceImpl---ysbPayment--");

            obj.put("code", CodeReturn.success);
            obj.put("info", "返回成功");

        } else {
            obj.put("code", CodeReturn.TOKEN_WRONG);
            obj.put("info", "该帐号已在别的设备登录，请重新登录");

        }
        return obj.toString();
    }

    @Override
    @Transactional
    public String callbackBackground(String result_code, String result_msg, String amount, String orderId,
                                     String mac) throws Exception {

        logger.error("第三方主动还款回调结果：" + "orderId:" + orderId + ",amount:" + amount + ",result_code:" + result_code + ",result_msg:" + result_msg);
        Order order = orderMapper.selectBySerial(orderId);
        // 获取商户号
        String accountId = PropertiesReaderUtil.read("third", "accountId");
        // 获取加密用的key
        String key = PropertiesReaderUtil.read("third", "key");
        StringBuffer macStr = new StringBuffer("accountId=" + accountId);
        macStr.append("&").append("orderId=" + orderId);
        macStr.append("&").append("amount=" + amount);
        macStr.append("&").append("result_code=" + result_code);
        macStr.append("&").append("result_msg=" + result_msg);
        macStr.append("&").append("key=" + key);
        // logger.error(macStr.toString());
        String mac1 = MD5Util.encodeToMd5(macStr.toString()).toUpperCase();

        //如果是测试 短路 默认都成功
        if ("on".equals(isTest)) {
            result_code = "0000";
            result_msg = "测试环境默认成功";
        }
        if (mac1.equals(mac)) {
            if ("0000".equals(result_code)) {
                if ("p".equals(order.getRlState())) {
//                    logger.info("第三方资金操作成功返回=" + result_code);
//                    logger.info("第三方资金操作成功返回orderId=" + orderId);
                    // -----------订单成功以后操作---------------
                    // 1.清结算
                    int settle = settlement(orderId, "s", result_msg);
                    String fee = "2.00";

                    amount = String.format("%.2f", Double.valueOf(order.getOptAmount()) + Double.valueOf(fee));

                    if (settle == 1) {
                        // 清结算成功
                        afterSettle(order, amount);
                    }
                }
            } else {
                if (!"f".equals(order.getRlState())) {
//                    logger.info("第三方资金操作失败返回" + result_code);

                    // -----------订单失败以后操作---------------
                    paymentFail(orderId, result_msg);
                }
            }
        } else {
            logger.error("签名验证失败，我们加密的mac==" + mac1 + ",他们返回的mac==" + mac);
        }

        return "";
    }

    @Override
    public String callbackBackgroundByNet(String orderJson) throws Exception {
        return null;
    }

    @Override
//    @Transactional
    public String payCont(String per_id, String borrId) throws Exception {
        JSONObject obj = new JSONObject();
        logger.info("进入----YsbPayServiceImpl---payCont--");
        try {
            // 幂等性操作 防止重复放款
            if (StringUtils.isEmpty(jedisCluster.get(RedisConst.PAYCONT_KEY + borrId))) {

                //缓存里有  set nx
                String setnx = jedisCluster.set(RedisConst.PAYCONT_KEY + borrId, borrId, "NX", "EX", 60 * 5);
                if (!"OK".equals(setnx)) {
                    // nx set成功了才能下一步  否则 返回重复
                    logger.error("直接返回，重复数据orderId" + borrId);
                    obj.put("code", CodeReturn.fail);
                    obj.put("info", "不可重复放款！");
                    obj.put("data", "");
                    return obj.toString();
                }
            } else {
                logger.error("直接返回，重复数据orderId" + borrId);
                obj.put("code", CodeReturn.fail);
                obj.put("info", "不可重复放款！");
                obj.put("data", "");
                return obj.toString();
            }



            // 获取合同信息
            BorrowList bo = borrMapper.selectByPrimaryKey(Integer.parseInt(borrId));
            // 获取用户信息
            Person p = personMapper.selectByPrimaryKey(bo.getPerId());
            if (CodeReturn.STATUS_COM_PAY_FAIL.equals(bo.getBorrStatus()) || CodeReturn.STATUS_SIGNED.equals(bo.getBorrStatus())) {

                //放款前验证下另一个系统用户是否借款

                String jine = bo.getBorrAmount().toString();
                // 算手续费...暂定每笔2块钱手续费，5万封顶
                String abc = "2.00";


                // 合同状态改为放款中
                bo.setBorrStatus(CodeReturn.STATUS_COM_PAYING);
                bo.setUpdateDate(new Date());
                int up1 = borrMapper.updateByPrimaryKeySelective(bo);
                logger.info("up1:" + up1);
                logger.info("bo:" + com.alibaba.fastjson.JSONObject.toJSONString(bo));


                // 获取还款详情
                RepaymentDetails rd = borrMapper.getRepaymentDetails(borrId);

                // 生成流水号
                String serialNo = SerialNumUtil.createByType("JHS01");

                Order order = new Order();
                order.setSerialNo(serialNo);
                order.setpId(0);
                order.setCompanyId(1);
                order.setPerId(bo.getPerId());
                order.setBankId(Integer.parseInt(rd.getBankId()));
                order.setContractId(Integer.parseInt(borrId));
                order.setOptAmount(String.valueOf(Double.parseDouble(jine)));
                order.setActAmount(String.valueOf(Double.parseDouble(jine)));
                order.setRlState("p");
                order.setType("1");
                order.setStatus("y");
                order.setCreationDate(new Date());
                order.setUpdateDate(new Date());
                orderMapper.insertSelective(order);
                // 手续费订单
                String serialNo1 = SerialNumUtil.createByType("JHS03");
                Order order1 = new Order();
                order1.setSerialNo(serialNo1);
                order1.setpId(order.getId());
                order1.setCompanyId(1);
                order1.setPerId(bo.getPerId());
                order1.setBankId(Integer.parseInt(rd.getBankId()));
                order1.setContractId(Integer.parseInt(borrId));
                order1.setOptAmount(abc);
                order1.setActAmount(abc);
                order1.setRlState("p");
                order1.setType("3");
                order1.setStatus("y");
                order1.setCreationDate(new Date());
                order1.setUpdateDate(new Date());
                orderMapper.insertSelective(order1);

                String name = p.getName();
                String cardNo = p.getBankCard();
                String orderId = order.getSerialNo();
                String purpose = "借款";
                String amount = jine;
                String responseUrl = PropertiesReaderUtil.read("third", "payContCallBackUrl");

                String ll = PaymentUtil.payCont(name, cardNo, orderId, purpose, amount, responseUrl);
                JSONObject obj1 = JSONObject.parseObject(ll);
                String result_code = obj1.getString("result_code");
                String result_msg = obj1.getString("result_msg");
                //测试默认全成功
                if ("on".equals(isTest)) {
                    obj1.put("result_code", "0000");
                    obj1.put("result_msg", "测试默认全成功");
                }

                if ("0000".equals(result_code)) {
                    obj.put("code", CodeReturn.success);
                    obj.put("info", "第三方受理成功");
                    obj.put("data", result_msg);
                }  else {
                    obj.put("code", CodeReturn.fail);
                    obj.put("info", result_msg);
                    obj.put("data", "");
                }

            } else {
                // 陈振让改的
                obj.put("code", CodeReturn.fail);
                obj.put("info", "系统异常，合同状态不符，请刷新页面！");
                obj.put("data", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            obj.put("code", CodeReturn.fail);
            obj.put("info", "系统出现错误");
            obj.put("data", "");
            throw e;
        }

        return obj.toString();
    }

    @Override
    @Transactional
    public String payContCallBack(String result_code, String result_msg, String amount, String orderId,
                                  String mac) throws Exception {
        logger.error("第三方代付返回结果" + "orderId:" + orderId + ",amount:" + amount + ",result_code:" + result_code + ",result_msg:" + result_msg);
        // 获取商户号
        String accountId = PropertiesReaderUtil.read("third", "accountId");
        // 获取加密用的key
        String key = PropertiesReaderUtil.read("third", "key");
        StringBuffer macStr = new StringBuffer("accountId=" + accountId);
        macStr.append("&").append("orderId=" + orderId);
        macStr.append("&").append("amount=" + amount);
        macStr.append("&").append("result_code=" + result_code);
        macStr.append("&").append("result_msg=" + result_msg);
        macStr.append("&").append("key=" + key);
        // logger.error(macStr.toString());
        String mac1 = MD5Util.encodeToMd5(macStr.toString()).toUpperCase();
        //如果是测试 短路 默认都成功
        if ("on".equals(isTest)) {
            result_code = "0000";
            result_msg = "测试环境默认成功";
        }
        if (mac1.equals(mac)) {
            if ("0000".equals(result_code)) {
                Order order = orderMapper.selectBySerial(orderId);
                if (!"s".equals(order.getRlState())) {
                    try {
                        successCaozuo(orderId);
                    } catch (Exception e) {
                        throw e;
                    }
                }
            } else {
                logger.info("返回信息==" + result_code + ",返回的内容==" + result_msg);
                //-------------2017.11.15 延迟队列----START----------------------------

                //-------------2017.11.15 延迟队列----END-------------------------------
                Order order = orderMapper.selectBySerial(orderId);

                if (!"f".equals(order.getRlState())) {
                    try {
                        fileCaozuo(orderId, result_msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.info(e);
                        throw e;
                    }
                }
            }
        } else {
            logger.error("签名验证失败，我们加密的mac==" + mac1 + ",他们返回的mac==" + mac);
        }
        return "";
    }

    @Override
    public void queryCall() {
        // 查询订单状态为p,订单类型为1代付，打款的数据，订单创建时间<任务运行的时间-30分钟
        logger.error("进入定时器查询放款的状态为处理中的订单======");
        // logger.error("0000000000000000000000000000000");
        List<Order> list = orderMapper.selectOrders("p", "1", 5);
        logger.error("type1:" + list.size());
        if (list.isEmpty()) {
            logger.info("queryCall没有放款中的订单");
        } else {
            for (Order order : list) {
                String orderId = order.getSerialNo();
                String response = PaymentUtil.queryOrderStatus(orderId);
//                JSONObject res = JSON.toJSON(response);
                com.alibaba.fastjson.JSONObject res = com.alibaba.fastjson.JSONObject.parseObject(response);
                String result_code = res.getString("result_code");
                String result_msg = res.getString("result_msg");
                String desc = res.getString("desc");
                //如果是测试 短路 默认都成功
                if ("on".equals(isTest)) {
                    result_code = "0000";
                    result_msg = "测试环境默认成功";
                    desc = "测试环境desc";
                }
                if ("0000".equals(result_code)) { // 查询成功

                    String status;
                    //如果是测试 短路 默认都成功
                    if ("on".equals(isTest)) {
                        status = "00";
                    } else {
                        status = res.getString("status");
                    }
                    if ("00".equals(status)) {
                        if (!"s".equals(order.getRlState())) {
                            try {
                                successCaozuo(orderId);
                            } catch (Exception e) {
                                logger.info(e);
                                e.printStackTrace();
                            }
                        }
                    } else if ("20".equals(status)) {
                        if (!"f".equals(order.getRlState())) {
                            try {
                                if (StringUtils.isNotEmpty(desc)) {
                                    fileCaozuo(orderId, desc);
                                } else {
                                    fileCaozuo(orderId, result_msg);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.info(e);
                            }
                        }
                    }
                } else if ("2002".equals(result_code)) {
                    //-------------2017.11.15 延迟队列----START----------------------------
                    //订单号不存在  进入延迟队列   重复调用4次
                    fileCaozuo(orderId, "订单不存在");
                    //-------------2017.11.15 延迟队列----END-------------------------------
                } else {
                    logger.info("流水号==" + order.getSerialNo() + "的订单查询失败,失败原因==" + result_msg);
                }
            }
        }
    }

    /**
     * 第三方操作成功进行的数据库操作
     *
     * @param orderId
     */
    @Transactional(rollbackFor = Exception.class)
    public void successCaozuo(String orderId) throws Exception {
        // 幂等操作，统一订单只处理一遍
        if (StringUtils.isEmpty(jedisCluster.get(RedisConst.ORDER_KEY + orderId))) {
            String setnx = jedisCluster.set(RedisConst.ORDER_KEY + orderId, orderId, "NX", "EX", 60 * 5);
            if (!"OK".equals(setnx)) {
                logger.error("直接返回，重复数据orderId" + orderId);
                return;
            }

        } else {
            logger.error("直接返回，重复数据orderId" + orderId);
            return;
        }


        // orderId = "0116120210154754988";
        // 修改订单状态为s，订单更新时间
        try {
            Order order = new Order();
            Order order1 = new Order();
            order = orderMapper.selectBySerial(orderId);
            order1 = orderMapper.selectByPid(order.getId());

            // 幂等操作，还款计划只生成一条
            if (StringUtils.isEmpty(jedisCluster.get(RedisConst.REPAYMENT_KEY + order.getContractId()))) {

                String setnx = jedisCluster.set(RedisConst.REPAYMENT_KEY + order.getContractId(), order.getContractId().toString(), "NX", "EX", 60 * 5);
                if (!"OK".equals(setnx)) {
                    logger.error("setnx失败，直接返回，重复数据还款计划，borrId" + order.getContractId());
                    return;
                }

            } else {
                logger.error("直接返回，重复数据还款计划，borrId" + order.getContractId());
                return;
            }


            order.setRlState("s");
            order.setRlDate(new Date());
            order.setUpdateDate(new Date());
            order1.setRlState("s");
            order1.setRlDate(new Date());
            order1.setUpdateDate(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            orderMapper.updateByPrimaryKeySelective(order1);
            logger.info("修改订单状态为s成功》插入一条资金记录开始...");


            PerAccountLog pal = new PerAccountLog();
            pal.setPerId(order.getPerId());
            pal.setOrderId(order.getId());
            pal.setOperationType(order.getType());
            pal.setAmount(order.getActAmount());
            pal.setRemark("收款");
            pal.setAddtime(new Date());
            palMapper.insertSelective(pal);
            logger.info("增加一条ym_per_account_log新增一条资金明系成功》修改合同状态开始...");


            // 获得合同信息
            Calendar calendar = new GregorianCalendar();
            BorrowList borr = borrMapper.selectByPrimaryKey(order.getContractId());
            // 合同和人的信息
            BorrPerInfo bpi = new BorrPerInfo();
            bpi = borrMapper.selectByBorrId(borr.getId());
            // 修改合同状态
            Date date = new Date();
            borr.setPayDate(date);
            calendar.setTime(date);
            calendar.add(Calendar.DATE, bpi.getTerm_value());// 把日期往后增加7天.整数往后推,负数往前移动
            date = calendar.getTime(); // 这个时间就是日期往后推一天的结果
            borr.setPlanrepayDate(date);
            borr.setBorrStatus("BS004");
            borr.setIspay(1);
            borr.setUpdateDate(new Date());
            borrMapper.updateByPrimaryKeySelective(borr);
            logger.info("修改合同状态成功》插入还款计划开始...");

            RepaymentDetails rd = new RepaymentDetails();
            rd = borrMapper.getRepaymentDetails(String.valueOf(borr.getId()));
            if (null == rd.getCouAmount() || "".equals(rd.getCouAmount())) {
                rd.setCouAmount("0.00");
            }
            logger.info("successCaozuo------borr:" + JSON.toJSON(borr));


            // 插入一条还款计划
            int insert = 0;
            //TODO
            if (insert == 0) {
                logger.error("插入还款计划失败！" + borr.getId());
            }
            logger.info("successCaozuo------RepaymentPlan:" + JSON.toJSON(borr));
            logger.info("插入还款计划成功》发送消息给用户开始...");
            // 发送消息给用户
            // UserServiceImpl us = new UserServiceImpl();
            DecimalFormat df = new DecimalFormat("######0.00");
            //String params=bpi.getName() + "," + df.format(bpi.getMaximum_amount()) + "元," + bpi.getTerm_value() + "天,"+ DateUtil.getDateString(date);
            String params=bpi.getName() + "," + df.format(bpi.getMaximum_amount()) + ","+borr.getDepositAmount();
            String result = userService.setMessage(String.valueOf(order.getPerId()), "2", params);
            JSONObject obje = (JSONObject) JSON.toJSON(result);
            if ("200".equals(obje.get("code"))) {
                logger.info("消息发送成功！");
            } else {
                logger.info(obje.get("info").toString());
            }
            //
           // String smsParams=bpi.getPhone()+","+bpi.getMaximum_amount()+","+borr.getDepositAmount();
            ResponseDo rspDo=smsService.sendSms(SmsTemplateEnum.LOAN_SUCCESS_REMIND.getCode(),bpi.getPhone(),bpi.getName(),df.format(bpi.getMaximum_amount()),df.format(borr.getDepositAmount()));
            if (StateCode.SUCCESS_CODE==rspDo.getCode()) {
                log.info("短信发送成功！");
            }else{
                log.info("短信发送失败！");
            }

            //成功需要生成电子合同
            electronicContractService.createElectronicContract(order.getContractId());

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 第三方操作失败进行的数据库操作
     *
     * @param orderId
     * @param result_msg
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fileCaozuo(String orderId, String result_msg) {
        // 修改订单状态为f，订单更新时间
        Order order = new Order();
        Order order1 = new Order();
        try {
            order = orderMapper.selectBySerial(orderId);
            order1 = orderMapper.selectByPid(order.getId());
            order.setRlState("f");
            order.setRlDate(new Date());
            order.setReason(result_msg);
            order.setUpdateDate(new Date());
            order1.setRlState("f");
            order1.setRlDate(new Date());
            order1.setReason(result_msg);
            order1.setUpdateDate(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            orderMapper.updateByPrimaryKeySelective(order1);
            // 合同状态改为放款失败(只有在借款状态为放款中时才能改为失败)
            BorrowList record = borrMapper.selectByPrimaryKey(order.getContractId());
            if (CodeReturn.STATUS_COM_PAYING.equals(record.getBorrStatus())){
                record.setBorrStatus(CodeReturn.STATUS_COM_PAY_FAIL);
                record.setUpdateDate(new Date());
                int i = borrMapper.updateByPrimaryKeySelective(record);
                if (i > 0) {
                    logger.error("更改借款状态为放款失败" + record.getId());
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public String queryPayment() {


        logger.error("进入定时器查询还款的状态为处理中的订单======");


        List<Order> list = orderMapper.selectOrders("p", "5", 5);

        logger.error("type5:" + list.size());

        String fee = "2.00";


        if (list.isEmpty()) {
            logger.info("queryPayment没有主动还款为p的订单");
        } else {

            for (Order order : list) {
                try {
                    String orderId = order.getSerialNo();

                    logger.error("处理还款订单流水号======>" + orderId);
                    String response = PaymentUtil.queryPayOrderStatus(orderId);
                    com.alibaba.fastjson.JSONObject res = com.alibaba.fastjson.JSONObject.parseObject(response);
                    String result_code = res.getString("result_code");
                    String result_msg = res.getString("result_msg");
                    String desc = "";
                    if (StringUtils.isNotEmpty(res.getString("desc"))) {
                        desc = res.getString("desc");
                    }
                    String amount = String.format("%.2f", Double.valueOf(order.getOptAmount()) + Double.valueOf(fee));


                    // -------------测试环境模拟银生宝------------------
                    if ("on".equals(isTest)) {
//                        //成功失败随机出现
//                        Random ran = new Random();
//                        if (ran.nextInt(4) > 1) {
//                            result_code = "0000";
//                        } else {
//                            result_code = "1111";
//                        }

                        //全部成功
                        result_code = "0000";
                        result_msg = "测试模拟";
                        desc = "测试模拟desc";
                    }
                    // -------------测试环境模拟银生宝-------------------
                    if ("0000".equals(result_code)) {// 查询成功
                        // -------------测试环境模拟银生宝------------------
                        String status = res.getString("status");

                        if ("on".equals(isTest)) {
//                            //三种结果随机出现
//                            Random random = new Random();
//                            int statusFlag = random.nextInt(8);
//                            if (statusFlag == 0) {
//                                status = "20";
//                            } else {
//                                status = "00";
//                            }
                            status = "00";

                        } else {
                            status = res.getString("status");
                        }
                        if ("00".equals(status)) {
                            if ("p".equals(order.getRlState())) {
                                logger.info("第三方认证支付操作成功返回=" + result_code);
                                logger.info("第三方认证支付操作成功返回orderId=" + orderId);
                                // -------------还款结果成功操作------------------
                                int settle = settlement(orderId, "s", desc);
                                if (settle == 1) {
                                    afterSettle(order, amount);
                                }

                            }
                        } else if ("10".equals(status)) {
                            logger.info("流水号==" + order.getSerialNo() + "的订单仍在处理中，返回消息" + desc);
                        } else if ("20".equals(status)) {
                            logger.info("第三方资金操作失败返回:" + "流水号==" + order.getSerialNo() + "的订单查询失败,失败原因==" + desc);

                            paymentFail(orderId, desc);
                        }
                    } else {
                        logger.info("第三方资金操作失败返回:" + "流水号==" + order.getSerialNo() + "的订单查询失败,失败原因==" + result_msg);
                        // -----------订单失败以后操作---------------
                        paymentFail(orderId, result_msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // continue;
                }
            }
        }
        return "";
    }

    // 2017.03.27---APP还款提交
    @SuppressWarnings("deprecation")
    @Override
    @Transactional(propagation = Propagation.NEVER)
    public NoteResult AppRepay(String per_id, String serial, String amount) {
        NoteResult result = new NoteResult("9999", "系统繁忙");

        //判断金额合法性  小于或等于0  直接拒绝
        if (Double.parseDouble(amount) <= 0) {
            return new NoteResult("9999", "金额不合法！");
        }

        BorrowList borr = borrMapper.selectNow(Integer.valueOf(per_id));
        try {

            //清结算锁
            String settleLock = jedisCluster.get(RedisConst.SETTLE_LOCK_KEY);
            if (!com.alibaba.dubbo.common.utils.StringUtils.isEmpty(settleLock) && "off".equals(settleLock)) {
                result.setCode(CodeReturn.FAIL_CODE);
                result.setInfo("凌晨0点-5点开始为系统维护时间，期间不能进行还款，给您带来的不便敬请谅解！");
                return result;
            }

//            if (!checkLock(borr.getId())){
//                result.setCode(CodeReturn.FAIL_CODE);
//                result.setInfo("有正在处理的还款，请不要重复还款");
//                return result;
//            }


            // 从快速编码表查出手续费 1：代收
            String fee = "2.00";

            String responseUrl = PropertiesReaderUtil.read("third", "zhifuCallBackUrl");


            logger.info("提交amount：" + amount + ",fee:" + fee + ",验证金额：" + (Double.parseDouble(amount) - Double.parseDouble(fee)));
            //  用户还款金额减去手续费不能超过应还金额
            NoteResult canPay = canPayCollect(String.valueOf(borr.getId()), (Double.parseDouble(amount) - Double.parseDouble(fee)));
            if (CodeReturn.FAIL_CODE.equals(canPay.getCode())) {
                result.setCode(canPay.getCode());
                result.setInfo(canPay.getInfo());
                return result;
            }

            String borrId = String.valueOf(borr.getId());

            Bank bank = bankMapper.selectByPerId(per_id);

            if(bank == null){
                return new NoteResult(CodeReturn.FAIL_CODE, "您尚未绑定银行卡主卡，请设置主卡后还款");
            }

            logger.info("进入----YsbPayServiceImpl---ysbPayment--");
            // 手续费
            String abc = fee;

            // 流水号
            String orderId = SerialNumUtil.createByType("JHS05");
            Order order = new Order();
            order.setSerialNo(orderId);
            order.setpId(0);
            order.setCompanyId(1);
            order.setPerId(Integer.parseInt(per_id));
            order.setBankId(bank.getId());
            order.setContractId(borr.getId());
            order.setOptAmount(String.valueOf(Double.parseDouble(amount) - Double.parseDouble(abc)));
            order.setActAmount(String.valueOf(Double.parseDouble(amount) - Double.parseDouble(abc)));
            order.setRlState("p");
            order.setType("5");
            order.setStatus("y");
            order.setCreationDate(new Date());
            order.setUpdateDate(new Date());

            String userSysno = collectorsListMapper.selectCollectUserByBorrId(borr.getId());

            if (StringUtils.isNotEmpty(userSysno)) {
                order.setCollectionUser(userSysno);
            }
            //insert之前set key
            if (!"OK".equals(jedisCluster.set(RedisConst.PAY_ORDER_KEY + borrId, "off", "NX", "EX", 2 * 60 * 60))) {
                return new NoteResult(CodeReturn.FAIL_CODE, "当前有还款在处理中，请稍后");
            }

            int i = orderMapper.insertSelective(order);


            // 修改：不等第三方受理成功，再生成手续费订单  直接生成
            Order feeOrder = new Order();
            feeOrder.setGuid(BorrNum_util.createBorrNum());
            // 手续费订单的pid为主订单id
            feeOrder.setpId(order.getId());
            feeOrder.setSerialNo("03" + BorrNum_util.createBorrNum());
            feeOrder.setPerId(order.getPerId());
            feeOrder.setCompanyId(order.getCompanyId());
            feeOrder.setContractId(order.getContractId());
            feeOrder.setOptAmount(fee);
            feeOrder.setActAmount(fee);
            feeOrder.setRlState("p");
            feeOrder.setType("3");// 手续费

            int j = orderMapper.insertSelective(feeOrder);

            if (i + j < 2) {
                //插入订单失败
                return new NoteResult("9999", "数据库错误");
            }

            //insert成功，delete key
            jedisCluster.del(RedisConst.PAY_ORDER_KEY + borrId);

            NoteResult requestResult = requestYsb(amount, bank.getPhone(), bank.getSubContractNum(), orderId, responseUrl);


            if ("0000".equals(requestResult.getCode())) {
                result.setCode("200");
                result.setInfo("请求已受理,请等候结果");
            } else {
                // 第三方受理失败
                order.setRlState("f");
                order.setRlRemark(requestResult.getInfo());
                orderMapper.updateByPrimaryKeySelective(order);
                feeOrder.setRlState("f");
                feeOrder.setRlRemark(requestResult.getInfo());
                orderMapper.updateByPrimaryKeySelective(feeOrder);
                result.setCode(requestResult.getCode());
                result.setInfo(requestResult.getInfo());

                // 合同和人的信息
                BorrPerInfo bpi = new BorrPerInfo();
                bpi = borrMapper.selectByBorrId(borr.getId());
                //还款失败时发送站内信
                //String params=bpi.getName() + "," + DateUtil.getDateStringToHHmmss(new Date()) + "," + amount + "," + requestResult.getInfo();
                String params=bpi.getName() + "," + DateUtil.getDateStringToHHmmss(new Date()) + "," + amount ;
                userService.setMessage(per_id, "9", params);


            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.setInfo("操作失败:信息不正确或网络繁忙");
            BorrPerInfo bpi = new BorrPerInfo();
            bpi = borrMapper.selectByBorrId(borr.getId());
            //还款失败时发送站内信
           // String params=bpi.getName() + "," + DateUtil.getDateStringToHHmmss(new Date()) + "," + amount + "," + "操作失败:信息不正确或网络繁忙";
            String params=bpi.getName() + "," + DateUtil.getDateStringToHHmmss(new Date()) + "," + amount ;
            userService.setMessage(per_id, "9", params);
            return result;
        }


    }

    /**
     * 扣款前验证是否可以扣款
     *
     * @param borrId
     * @return
     */
    @Override
    public NoteResult canPayCollect(String borrId, double thisAmount) {

        NoteResult result = new NoteResult(CodeReturn.FAIL_CODE, "失败");
        try {
            // 查出应还金额减去p状态还款订单总额=剩余可还金额
            double canPay = rpMapper.selectCanPay(borrId);
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
            logger.error("出错：",e);
            return new NoteResult(CodeReturn.FAIL_CODE, "失败");
        }

    }

//    @Override
//    public NoteResult testBeike() {
//        beikeqianbao("13341814726","2017-08-24 08:00:00","100000","2017-08-25 08:00:00","90000");
//        return new NoteResult("1", "fuck");
//    }

    @Override
    public void queryQ() {
    }

    /**
     * 封装清结算服务
     *
     * @param orderId
     * @param status
     * @param msg
     * @return
     */
    @Override
    public int settlement(String orderId, String status, String msg) {
//        // 拿流水去清结算
//        GuestRepaySettlementRequest req = new GuestRepaySettlementRequest();
//        req.setOrderId(orderId);
//        req.setStatus(status);
//        req.setMsg(msg);
//        logger.info("清结算参数:" + "orderId:" + orderId + ",status:" + status + ",msg:" + msg);
//        ResponseUtil<GuestRepaySettlementResponse> response = rMISettlementService.guestRepaySettlement(req);
//        logger.info("清结算返回:" + orderId + JSON.toJSON(response).toString());
//        return response.getStatus();
        return 0;

    }

    /**
     * 清结算以后相关操作（资金流水 站内信 优惠券 等 ）
     *
     * @return
     */
    public void afterSettle(Order order, String amount) {

        Integer borrId = order.getContractId();
        BorrowList bo = borrMapper.selectByPrimaryKey(borrId);
        BorrPerInfo bpi = this.borrMapper.selectByBorrId(borrId);

        DecimalFormat df = new DecimalFormat("######0.00");

        //资金流水
        PerAccountLog pal = new PerAccountLog();
        pal.setPerId(order.getPerId());
        pal.setOrderId(order.getId());
        pal.setOperationType(order.getType());
        pal.setAmount(order.getOptAmount());
        pal.setRemark("还款");
        pal.setAddtime(new Date());
        this.palMapper.insertSelective(pal);
        logger.info("增加一条ym_per_account_log新增一条资金明细成功》..." + "bo:" + com.alibaba.fastjson.JSONObject.toJSONString(bo));


        if ("BS006".equals(bo.getBorrStatus())) {

            logger.info("插入一条优惠券==");
            if (bo.getActRepayDate().before(bo.getPlanrepayDate())) {
                String couponName = "";
                int prodId = bo.getProdId();
                switch (prodId) {
                    case 1:
                        couponName = "正常还款500元7天";
                        break;
                    case 2:
                        couponName = "正常还款500元14天";
                        break;
                    case 3:
                        couponName = "正常还款1000元7天";
                        break;
                    case 4:
                        couponName = "正常还款1000元14天";
                }
            }
        }
        logger.info("发送站内信通知用户还款成功！------");

        String time = String.format("%tF", new Date());
       // String message = "【悠米闪借】尊敬的用户，您于" + time + "已成功回 款" + amount + "元，感谢您的支持！";
      //  if (!"on".equals(isTest)) {//正式环境才发短信
       //     EmaySmsUtil.send(message, String.valueOf(bpi.getPhone()), 1);
      //  }
        smsService.sendSms(SmsTemplateEnum.REPAY_SUCCESS_REMIND.getCode(),bpi.getPhone(),time,df.format(amount));
        String result = "";
        if ("BS006".equals(bo.getBorrStatus()) || "BS010".equals(bo.getBorrStatus()) || "BS014".equals(bo.getBorrStatus())) {
            //String params=bpi.getName() + "," + df.format(bpi.getMaximum_amount());
            String params=bpi.getName() + ","+DateUtil.getDateString(new Date())+"," + df.format(bpi.getMaximum_amount());
            result = this.userService.setMessage(String.valueOf(bo.getPerId()), "5", params);
            logger.info("逾期结清，更新催收人状态，合同单号：" + order.getContractId());
            //逾期结清 算业绩
            int yeji = collectorsListMapper.updateCollectorsList(order.getContractId());
            if (yeji > 0) {
                logger.info("结清算业绩成功");
            }else{
                logger.info("合同结清，但催收人状态未改变，合同单号：" + order.getContractId());
            }

        } else {
            //String params=bpi.getName() + "," + time + ","+ df.format(Double.parseDouble(amount));
            String params=time + "," + df.format(Double.parseDouble(amount));
            result = userService.setMessage(String.valueOf(order.getPerId()), "10", params);
        }
        JSONObject obje = (JSONObject) JSON.toJSON(result);

        logger.info(obje.toString());
        if ("200".equals(obje.get("code").toString())) {
            logger.info("还款成功消息发送成功！");
        } else {
            logger.info(obje.get("info").toString());
        }
    }


    public NoteResult requestYsb(String amount, String phone, String sub, String serial, String responseUrl) throws Exception {

        NoteResult result = new NoteResult();
        // ---------------------------------真实第三方--------------------------------------------------
        String response = CollectUtil.requestCollect(amount, phone, sub, serial, responseUrl);
        com.alibaba.fastjson.JSONObject res = com.alibaba.fastjson.JSONObject.parseObject(response);
        result.setCode(res.getString("result_code"));
        result.setInfo(res.getString("result_msg"));
        // ----------------------------------模拟第三方-------------------------------------------------
        if ("on".equals(isTest)) {
            result.setCode("0000");
            result.setInfo("模拟第三方");
        }
        // ------------------------------------------------------------------------------
        return result;


    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    private  void beikeqianbao(String phone, String apply_time, String apply_money, String loan_time, String loan_money) {

    }


    /*
        主动还款失败统一方法
     */
    @Override
    @Transactional
    public void paymentFail(String orderId, String desc) {
        // -----------订单失败以后操作---------------
        int settle = settlement(orderId, "f", desc);
        if (settle == 1) {

            Order order = orderMapper.selectBySerial(orderId);
            // 还款失败时发送站内信
           // String params=bpi.getName() + "," + DateUtil.getDateStringToHHmmss(order.getCreationDate()) + ","+ order.getOptAmount() + "," + desc;
            String params=DateUtil.getDateStringToHHmmss(order.getCreationDate()) + "," + order.getOptAmount() ;
            userService.setMessage(order.getPerId() + "", "9", params);
        }
    }


}
