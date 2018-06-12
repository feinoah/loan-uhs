package com.jhh.jhs.loan.service.haier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.capital.HaierCallBackService;
import com.jhh.jhs.loan.common.util.DateUtil;
import com.jhh.jhs.loan.common.util.RedisConst;
import com.jhh.jhs.loan.common.util.SerialNumUtil;
import com.jhh.jhs.loan.constant.HaierConstants;
import com.jhh.jhs.loan.entity.HaierBaseVo;
import com.jhh.jhs.loan.entity.HaierQueryVo;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.mapper.gen.LoanOrderDOMapper;
import com.jhh.jhs.loan.mapper.gen.domain.LoanOrderDO;
import com.jhh.jhs.loan.mapper.gen.domain.LoanOrderDOExample;
import com.jhh.jhs.loan.service.capital.SettlementUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

;

/**
 * 代扣代付 回调  查询
 */
public class HaierCallBackServiceImpl extends HaierBaseServiceImpl implements HaierCallBackService {
    private static final Logger logger = LoggerFactory
            .getLogger(HaierCallBackServiceImpl.class);

    @Autowired
    private SettlementUtil settlementUtil;

    @Autowired
    private LoanOrderDOMapper loanOrderDOMapper;

    @Autowired
    private JedisCluster jedisCluster;


    /**
     * 代扣定时查询接口
     *
     * @param
     * @return
     * @throws Exception
     */
    @Override
    public NoteResult queryTrade() {
//        logger.info("--------------------->>>>>>>> 代扣查询HaierCallBankServiceImpl.query_trade--------------<<<<<<<<<\n");
        NoteResult noteResult = new NoteResult(HaierConstants.WAIT, HaierConstants.PROCESS);
        HaierQueryVo trade = new HaierQueryVo();
        logger.info("进入定时器查询代扣的状态为处理中的订单======");
        //查询订单状态为p,订单类型为4还款（代收）的数据，订单创建时间<任务运行的时间-30分钟
        List<LoanOrderDO> loanOrderDOList;
        try {
            LoanOrderDOExample loanOrderDOExample = new LoanOrderDOExample();
            LoanOrderDOExample.Criteria cia = loanOrderDOExample.createCriteria();
            List<String> list = new ArrayList<>();
            list.add("13");
            list.add("14");
            cia.andTypeIn(list);
            cia.andRlStateEqualTo("p");
            Calendar beforeTime = Calendar.getInstance();
            beforeTime.add(Calendar.MINUTE, -5);
            cia.andCreationDateLessThan(beforeTime.getTime());
            loanOrderDOList = loanOrderDOMapper.selectByExample(loanOrderDOExample);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("queryTrade=====定时任务查询数据库产生异常！");
            return noteResult;
        }
        if (loanOrderDOList.isEmpty()) {
            //没有p的订单 不查询
            noteResult.setInfo("queryTrade没有可查询订单");
            logger.info("queryTrade没有p状态的代扣订单");
        } else {
            for (LoanOrderDO order : loanOrderDOList) {
                if (!"OK".equals(jedisCluster.set(RedisConst.PAY_ORDER_KEY + order.getSerialNo(), "off", "NX", "EX", 1 * 60 * 60))) {
                    logger.error("该订单正在处理中,订单serNo:{}", order.getSerialNo());
                    continue;
                }
                try {
                    String serialNo = order.getSerialNo();
                    trade.setOut_trade_no(serialNo);
                    JSONObject bizReq = (JSONObject) JSON.toJSON(trade);
                    logger.info("基本请求参数转换json为 bizReq = " + bizReq);
                    HaierBaseVo base = convertRequestBaseParm(bizReq);
                    //------------------------真实请求第三方查询---------------
                    String response = post(base);
                    //判断订单是否查询成功
                    noteResult = trade_success(response);
                    //-----------------------------------------------------
                    if (HaierConstants.SUCCESS.equals(noteResult.getCode())) { //第三方受理成功
                         handleSuccess(order);
                    } else if (HaierConstants.FILE.equals(noteResult.getCode())) {
                        handleFail(order, noteResult.getInfo());
                    }
                    jedisCluster.del(RedisConst.PAY_ORDER_KEY + order.getSerialNo());
                } catch (Exception e2) {
                    logger.error("queryTrade=====定时任务循环查询时报错！订单号：" + order.getSerialNo(), e2);
                    jedisCluster.del(RedisConst.PAY_ORDER_KEY + order.getSerialNo());

                }
            }
        }
        noteResult.setCode(HaierConstants.SUCCESS);
        return noteResult;
    }

    /**
     * 主动还款定时查询接口
     *
     * @param
     * @return
     * @throws Exception
     */
    @Override
    public NoteResult queryAppRpay() {
//        logger.info("--------------------->>>>>>>> 代扣查询HaierCallBankServiceImpl.query_trade--------------<<<<<<<<<\n");
        NoteResult noteResult = new NoteResult(HaierConstants.WAIT, HaierConstants.PROCESS);
        HaierQueryVo vo = new HaierQueryVo();
        //组转参数
        logger.info("进入定时器查询主动还款的状态为处理中的订单======");
        //查询订单状态为p,订单类型为4还款（代收）的数据，订单创建时间<任务运行的时间-30分钟
        List<LoanOrderDO> loanOrderDOList;
        try {
            LoanOrderDOExample loanOrderDOExample = new LoanOrderDOExample();
            LoanOrderDOExample.Criteria cia = loanOrderDOExample.createCriteria();
            List<String> list = new ArrayList<>();
            list.add("12");
            cia.andTypeIn(list);
            cia.andRlStateEqualTo("p");
            Calendar beforeTime = Calendar.getInstance();
            beforeTime.add(Calendar.MINUTE, -5);
            cia.andCreationDateLessThan(beforeTime.getTime());
            loanOrderDOList = loanOrderDOMapper.selectByExample(loanOrderDOExample);
            logger.info("主动还款loanOrderDOList:" + list.size());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("queryAppRpay=====定时任务查询数据库产生异常！");
            return noteResult;
        }
        if (loanOrderDOList.isEmpty()) {
            //没有p的订单 不查询
            noteResult.setInfo("queryAppRpay没有可查询订单");
            logger.info("queryAppRpay没有p状态的代扣订单");
        } else {
            for (LoanOrderDO order : loanOrderDOList) {
                try {
                    String serialNo = order.getSerialNo();
                    vo.setOut_trade_no(serialNo);
                    JSONObject bizReq = (JSONObject) JSON.toJSON(vo);
                    HaierBaseVo base = convertRequestBaseParm(bizReq);
                    //------------------------真实请求第三方查询---------------
                    String response = post(base);
                    //判断订单是否查询成功
                    noteResult = trade_success(response);

                    //-----------------------------------------------------
                    if (HaierConstants.SUCCESS.equals(noteResult.getCode())) { //第三方受理成功
                        handleSuccess(order);
                        logger.info("主动还款成功结果处理成功");
                    } else if (HaierConstants.FILE.equals(noteResult.getCode())) {
                        handleFail(order, noteResult.getInfo());
                        logger.info("主动还款失败结果处理成功");
                    }
                } catch (Exception e2) {
                    logger.error("queryAppRpay=====定时任务循环查询时报错！订单号：" + order.getSerialNo());
                    e2.printStackTrace();

                }
            }
        }
        noteResult.setCode(HaierConstants.SUCCESS);
        return noteResult;
    }


    /**
     * 单笔查询订单
     *
     * @param serialNo
     * @return
     */
    @Transactional
    @Override
    public NoteResult orderStatus(String serialNo) {
        NoteResult result;
        try {
            LoanOrderDO loanOrderDO = loanOrderDOMapper.selectBySerNo(serialNo);
            //组装请求参数
            HaierQueryVo vo = new HaierQueryVo();
            vo.setOut_trade_no(serialNo);
            JSONObject bizReq = (JSONObject) JSON.toJSON(vo);
            HaierBaseVo base = convertRequestBaseParm(bizReq);
            //------------------------真实请求第三方查询---------------
            String response = post(base);
            //判断订单是否查询成功
            result = trade_success(response);
            //-----------------------------------------------------
            if (HaierConstants.SUCCESS.equals(result.getCode())) {//第三方最终受理成功
                logger.info(serialNo + "成功结果处理成功");
                result.setCode(HaierConstants.SUCCESS_OO);
                result.setInfo(HaierConstants.SUCESS_VALUE);
            } else if (HaierConstants.FILE.equals(result.getCode())) {
                logger.info(serialNo + "失败结果处理成功");
                result.setCode(HaierConstants.SUCCESS_20);
                result.setInfo(result.getInfo());
            } else {
                logger.info(serialNo + "处理中");
                result.setCode(HaierConstants.SUCCESS_1O);
                result.setInfo(result.getInfo());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new NoteResult(HaierConstants.SUCCESS_1O, HaierConstants.SEARCH_ERROR);
        }
        return result;
    }

    /**
     * 单笔查询订单
     *
     * @param serialNo
     * @return
     */
    @Transactional
    @Override
    public NoteResult payOrderStatus(String serialNo) {
        NoteResult result;
        try {
            //组装请求参数
            HaierQueryVo vo = new HaierQueryVo();
            vo.setOut_trade_no(serialNo);
            JSONObject bizReq = (JSONObject) JSON.toJSON(vo);
            HaierBaseVo base = convertRequestBaseParm(bizReq);
            //------------------------真实请求第三方查询---------------
            String response = post(base);
            //判断订单是否查询成功
            result = trade_success(response);
            //-----------------------------------------------------
            if (HaierConstants.SUCCESS.equals(result.getCode())) {//第三方最终受理成功
                logger.info(serialNo + "成功结果处理成功");
                result.setCode(HaierConstants.SUCCESS_OO);
                result.setInfo(HaierConstants.SUCESS_VALUE);
            } else if (HaierConstants.FILE.equals(result.getCode())) {
                logger.info(serialNo + "失败结果处理成功");
                result.setCode(HaierConstants.SUCCESS_20);
                result.setInfo(result.getInfo());
            } else {
                logger.info(serialNo + "处理中");
                result.setCode(HaierConstants.SUCCESS_1O);
                result.setInfo(result.getInfo());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new NoteResult(HaierConstants.SUCCESS_1O, HaierConstants.SEARCH_ERROR);
        }
        return result;
    }


    /**
     * 判断代扣否请求成功
     */
    private NoteResult trade_success(String response) {
        NoteResult noteResult = new NoteResult(HaierConstants.WAIT, HaierConstants.SEARCH_ERROR);
        //返回格式转换
        JSONObject result = JSONObject.parseObject(response);
        String code = result.getString("code");
        if (HaierConstants.SUCCESS_CODE.equals(code)) {
            String status = result.getJSONObject("biz_content").getString("status");
            //判断订单是否受理成功
            if (HaierConstants.TRADE_FINISHED.equals(status)) { //成功
                noteResult.setCode(HaierConstants.SUCCESS);
                noteResult.setInfo(HaierConstants.SUCESS_VALUE);
            } else if (HaierConstants.PAY_FINISHED.equals(status)) { //成功
                noteResult.setCode(HaierConstants.SUCCESS);
                noteResult.setInfo(HaierConstants.SUCESS_VALUE);
            } else if (HaierConstants.PAY_FAILED.equals(status)) { //成功
                noteResult.setCode(HaierConstants.FILE);
                noteResult.setInfo(result.getString("sub_msg"));
            } else if (HaierConstants.PAY_SUBMITTED.equals(status)) { //成功
                noteResult.setCode(HaierConstants.WAIT);
                noteResult.setInfo(HaierConstants.PAY_SUBMITTED_VALUE);
            } else if (HaierConstants.TRADE_CLOSED.equals(status)) {//失败
                noteResult.setCode(HaierConstants.FILE);
                noteResult.setInfo(result.getString("sub_msg"));
            } else {
                noteResult.setInfo(status + result.getString("sub_msg"));
            }
        } else {
            noteResult.setCode(HaierConstants.FILE);
            noteResult.setInfo(result.getString("sub_msg"));
        }
        return noteResult;
    }


    /**
     * 组装请求第三方参数
     */
    private HaierBaseVo convertRequestBaseParm(JSONObject bizReq) {
        HaierBaseVo base = new HaierBaseVo();
        if (bizReq != null) {
            base.setService(HaierConstants.QUERY_TRADE);
            base.setRequest_no(SerialNumUtil.createByType(""));
            base.setTimestamp(DateUtil.getDateStringToHHmmss(new Date()));
            String biz_content = encrypt(base.getSign_type(), bizReq.toString(), base.getCharset());
            logger.info("代扣基本参数加密串为 biz_content" + biz_content);
            base.setBiz_content(biz_content);
        }
        return base;
    }

}
