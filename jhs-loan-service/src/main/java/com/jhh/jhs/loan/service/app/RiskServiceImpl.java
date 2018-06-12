package com.jhh.jhs.loan.service.app;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.app.RiskService;
import com.jhh.jhs.loan.common.util.CodeReturn;
import com.jhh.jhs.loan.common.util.RedisConst;
import com.jhh.jhs.loan.constant.NodeConstant;
import com.jhh.jhs.loan.entity.app.*;
import com.jhh.jhs.loan.mapper.app.*;
import com.jinhuhang.risk.dto.*;
import com.jinhuhang.risk.dto.zhima.jsonbean.ZhiMaAuthorizeDto;
import com.jinhuhang.risk.dto.zhima.jsonbean.ZhiMaResult;
import com.jinhuhang.risk.dto.zhima.jsonbean.ZhiMaRiskDto;
import com.jinhuhang.risk.service.RiskAPI;
import com.jinhuhang.risk.service.impl.RiskAPIClient;
import com.jinhuhang.risk.service.impl.blacklist.BlacklistAPIClient;
import com.jinhuhang.risk.service.impl.zhima.ZhiMaCreditApiClient;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.JedisCluster;

import java.util.*;


/**认证service
 * @author xuepengfei
 */
@Service
public class RiskServiceImpl implements RiskService {

    private static final String SUCCESS_CODE = CodeReturn.SUCCESS_CODE;
    private static final String SUCCESS_INFO = "成功";
    private static final String FAIL_CODE = CodeReturn.FAIL_CODE;
    private static final String FAIL_INFO = "失败";
    private static final String RISK_SUCCESS_CODE = CodeReturn.RISK_SUCCESS_CODE;

    //认证状态编码
    private static final String STATUS_BPM_N = CodeReturn.STATUS_BPM_N;//未认证
    private static final String STATUS_BPM_Y = CodeReturn.STATUS_BPM_Y;//已认证
    private static final String STATUS_BPM_FAIL = CodeReturn.STATUS_BPM_FAIL;//认证失败
    private static final String STATUS_BPM_FAIL_B = CodeReturn.STATUS_BPM_FAIL_B;//认证失败且进黑名单
    private static final String STATUS_BPM_UP = CodeReturn.STATUS_BPM_UP;//已提交,仅手机认证节点有次状态

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

    //认证状态常量
    private static final String BPM_UNDO_CODE = CodeReturn.BPM_UNDO_CODE;
    //未认证
    private static final String BPM_UNDO_INFO = "未认证";
    //已认证
    private static final String BPM_FINISH_CODE = CodeReturn.BPM_FINISH_CODE;
    private static final String BPM_FINISH_INFO = "已认证";


    //芝麻Code
    private static final String ZHIMA_REFUSE_CODE = "203";
    private static final String ZHIMA_TOURL_CODE = "202";

    @Autowired
    private BpmNodeMapper bpmNodeMapper;
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private JedisCluster jedisCluster;
    @Autowired
    private BorrowListMapper borrowListMapper;
    @Autowired
    private ZhiMaMapper zhiMaMapper;
    @Autowired
    private SensetimeMapper sensetimeMapper;
    @Autowired
    private CodeValueMapper codeValueMapper;

    private BlacklistAPIClient blacklistAPIClient = new BlacklistAPIClient();

    private static final Logger logger = LoggerFactory.getLogger(RiskServiceImpl.class);

    @Value("${productId}")
    private String productId;

    /**
     * 插入个人信息后，走风控模型 java接口
     * @param per_id 用户ID
     * @param email 邮箱
     * @param tokenKey tokenkey
     * @param device 设备型号
     */
    @Override
    public NoteResult javaCheckRisk(String per_id, String email, String tokenKey, String device) {
        NoteResult result = new NoteResult(FAIL_CODE,"系统繁忙");
        //调用java个人认证参数
        Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
        if(person == null){
            result.setInfo("用户信息不存在或不完整");
            return result;
        }
        //风控参数拼接
        PersonAuthDto personAuthDto = new PersonAuthDto();
        personAuthDto.setIdCard(person.getCardNum());
        personAuthDto.setName(person.getName());
        personAuthDto.setPhone(person.getPhone());
        personAuthDto.setRequestId(String.valueOf(System.currentTimeMillis()));
        personAuthDto.setPlatform(device);
        personAuthDto.setTokenKey(tokenKey);
        FeatureDto featureDto = new FeatureDto();
        // 兼容老版本，若tokenkey为空，则不走白骑士
        if (StringUtils.isEmpty(tokenKey) || StringUtils.isEmpty(device)) {
            featureDto.setOrgCodeList("basic,yxshare,cis,tongdun");
        }else {
            featureDto.setOrgCodeList("basic,yxshare,cis,tongdun,bqs");
        }
        featureDto.setNodeCode(NodeConstant.PERSON_RISK_NODE);
        featureDto.setWhiteTag(0);

        //请求风控系统
        logger.info("个人认证请求参数为--------personAuthDto:"+JSONObject.toJSONString(personAuthDto)+",featureDto:"+JSONObject.toJSONString(featureDto));
        String response;
        try {
            RiskAPI riskApi = new RiskAPIClient();
            response = riskApi.risk(Integer.valueOf(productId), personAuthDto, featureDto);
        }catch (Exception e){
            logger.error("-------个人认证请求异常\n"+e);
            return result;
        }
        logger.info("个人认证返回结果为----------------------response"+response);
        if (response == null){
            logger.error("-------个人认证请求dubbo服务调用失败"+response);
            return result;
        }
        JSONObject obj = JSONObject.parseObject(response);
        //返回的报文中
        String code = obj.getString("code");
        String message = obj.getString("msg");
        if(RISK_SUCCESS_CODE.equals(code)){
            //风控结果成功，新增成功认证流程明细
            createBpmNode(per_id, NodeConstant.PERSON_RISK_NODE_ID, STATUS_BPM_Y,"");
            result.setCode(SUCCESS_CODE);
            result.setInfo(SUCCESS_INFO);

        }else if(CodeReturn.JuLiXinCode.RC_RULE.equals(code)){
            //更新节点
            createBpmNode(per_id, NodeConstant.PERSON_RISK_NODE_ID, STATUS_BPM_Y,message);
            result.setCode(SUCCESS_CODE);
            result.setInfo(SUCCESS_INFO);
            return result;
        } else if(CodeReturn.JuLiXinCode.RISK_FAIL_CODE.equals(code)){
            //风控结果为拒绝
            //新增认证失败流程明细
            createBpmNode(per_id, NodeConstant.PERSON_RISK_NODE_ID, STATUS_BPM_FAIL,code+message);
            //更改借款状态为审核未通过
            BorrowList borr = borrowListMapper.selectNow((Integer.valueOf(per_id)));
            borr.setBorrStatus(STATUS_REVIEW_FAIL);
            int j = borrowListMapper.updateByPrimaryKeySelective(borr);
            //更改借款状态成功，且新增认证失败流程明细成功
            if(j>0){
                result.setCode("220");
                return result;
            }
        }else {
            result.setCode(code);
            result.setInfo(message);
        }
        return result;
    }

    /**
     * 流程节点改变，并更新缓存(此节点没有则新增，此节点有则更新)
     * @param per_id 用户id
     * @param node_id 节点id
     * @param node_status 节点状态
     * @param description 节点描述
     * @return
     */
    @Override
    public NoteResult createBpmNode(String per_id,int node_id,String node_status,String description){
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);
        try {

            // 新增流程明细 如果有该明细 update
            BpmNode oldNode = bpmNodeMapper.selectByPerNode(Integer.valueOf(per_id), node_id);
            BpmNode node = oldNode==null ? new BpmNode() : oldNode;

            node.setPerId(Integer.valueOf(per_id));
            node.setNodeId(node_id);
            node.setNodeStatus(node_status);
            node.setDescription(description);
            node.setUpdateDate(new Date());

            logger.info("更新的节点："+JSONObject.toJSONString(node));

            int m = oldNode==null ? bpmNodeMapper.insertSelective(node) : bpmNodeMapper.updateByPrimaryKeySelective(node);

            if (m > 0) {
                //修改节点之后  都set一遍redis
                node.setUpdateDate(new Date());
                jedisHset(per_id, RedisConst.NODE_KEY + per_id,String.valueOf(node_id),JSONObject.toJSONString(node));
                result.setCode(SUCCESS_CODE);
                result.setInfo(SUCCESS_INFO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new NoteResult(FAIL_CODE, FAIL_INFO);
        }
        return result;
    }

    /**
      统一的hset方法   hset之前要判断key是否存在，如果key不存在会有问题。
     */
    @Override
    public void jedisHset(String per_id,String key,String field,String value){
        if(!jedisCluster.exists(key)){
            Person p = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
            initNode(p);
        }
        jedisCluster.hset(key, field, value);
    }

    /**
     * 初始化用户节点到缓存
     * @return 用户所有节点信息
     */
    @Override
    public Map<String, String> initNode(Person p){
        Map<String, String> nodeMap = jedisCluster.hgetAll(RedisConst.NODE_KEY + p.getId());


        if (nodeMap.isEmpty()) {
            // 缓存中没有用户信息 查出用户所有节点
            logger.info("缓存中没有用户信息 查出用户所有节点");
            List<BpmNode> nodes = null;
            List<String> ids = bpmNodeMapper.selectMaxNodeId("per_id", p.getId());
            if(ids!=null && !ids.isEmpty()) {
                nodes = bpmNodeMapper.selectAllNodes(ids);
                logger.info("新用户,节点数为：" + nodes.size());
            }

            if(nodes!=null) {
                //查出节点后生成map 存入缓存
                for (BpmNode n : nodes) {
                    nodeMap.put(n.getNodeId().toString(), JSONObject.toJSONString(n));
                }
            }
            if (!nodeMap.isEmpty()) {
                jedisCluster.hmset(RedisConst.NODE_KEY + p.getId(), nodeMap);
                jedisCluster.expire(RedisConst.NODE_KEY + p.getId(), 7 * 24 * 60 * 60);
            }


        }

        TreeMap<String, String> treeMap = new TreeMap<String, String>(nodeMap);
        return treeMap;
    }

    /**
     * 检查用户是否认证完成
     * @param per_id 用户ID
     * @return
     */
    @Override
    public NoteResult checkBpm(String per_id) {
        // 构建结果对象
        NoteResult result = new NoteResult();

        // 所有节点
        int[] nodeIds = getBpmList();
        // 设定一个开关 用来标记是否有状态为提交中的节点 目前暂时针对聚信立
        boolean haveNS004 = false;

        // 时间参数
        long now = System.currentTimeMillis();
        // 30天有效期
        long limit30 = 30 * 24 * 60 * 60 * 1000L;
        // 60天有效期
        long limit60 = 60 * 24 * 60 * 60 * 1000L;

        // 先查出当前person
        Person p = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
        if(ObjectUtils.isEmpty(p)){
            result.setCode(CodeReturn.FAIL_CODE);
            result.setInfo("用户不存在");
            result.setData(null);
            return result;
        }

        // 判断黑名单
        if(!isBlack(p.getPhone(), p.getCardNum())) {
            result.setCode(BPM_UNDO_CODE);
            result.setInfo("黑名单");
            result.setData("r");
            return result;
        }

        Map<String, String> nodeMap = initNode(p);
        logger.info(String.format("当前per_id【%s】,认证节点状态【%s】", per_id, nodeMap));

        for (String key : nodeMap.keySet()) {
            BpmNode node = JSONObject.toJavaObject(JSONObject.parseObject(nodeMap.get(key)), BpmNode.class);
            logger.info(String.format("当前per_id【%s】,认证节点【%s】,状态【%s】,详情【%s】", per_id, key, node.getNodeStatus(), node));

            // 节点状态NS001 未认证，直接返回未认证
            if (STATUS_BPM_N.equals(node.getNodeStatus())) {
                result.setCode(BPM_UNDO_CODE);
                result.setInfo(BPM_UNDO_INFO);
                result.setData(key);
                return result;
            }

            // 节点状态NS003 认证失败 检查认证时间 小于一个月返回黄框 超过一个月可重新认证
            if (STATUS_BPM_FAIL.equals(node.getNodeStatus()) || STATUS_BPM_FAIL_B.equals(node.getNodeStatus())) {
                if (now - node.getUpdateDate().getTime() < limit30) {
                    result.setCode(BPM_UNDO_CODE);
                    result.setInfo(BPM_UNDO_INFO);
                    result.setData("r");
                    return result;
                } else {
                    result.setCode(BPM_UNDO_CODE);
                    result.setInfo(BPM_UNDO_INFO);
                    result.setData(key);
                    return result;
                }
            }

            //节点2
            if (STATUS_BPM_Y.equals(node.getNodeStatus()) && "2".equals(key) && ArrayUtils.contains(nodeIds,2) && (p.getEndDate() != null) && (now- 24*60*60*1000 - p.getEndDate().getTime() > 0L)) {
                node.setNodeStatus("NS001");
                node.setUpdateDate(new Date());
                BpmNode node1 = bpmNodeMapper.selectByPerNode(Integer.valueOf(per_id), 1);
                node1.setNodeStatus("NS001");
                node1.setUpdateDate(new Date());
                this.bpmNodeMapper.updateByPrimaryKeySelective(node);
                this.bpmNodeMapper.updateByPrimaryKeySelective(node1);
                jedisHset(per_id,RedisConst.NODE_KEY + per_id, "1",JSONObject.toJSONString(node1));
                jedisHset(per_id,RedisConst.NODE_KEY + per_id, "2",JSONObject.toJSONString(node));
                result.setCode(BPM_UNDO_CODE);
                result.setInfo(BPM_UNDO_INFO);
                result.setData(key);
                return result;
            }

            //节点3 芝麻信用 认证状态为成功  并且更新时间大于1个月
            if (STATUS_BPM_Y.equals(node.getNodeStatus()) && "3".equals(key) && ArrayUtils.contains(nodeIds,3) && now - node.getUpdateDate().getTime() > limit30) {
                result.setCode(BPM_UNDO_CODE);
                result.setInfo(BPM_UNDO_INFO);
                result.setData(key);
                return result;
            }

            //节点4 通讯录 认证状态为成功 并且更新时间大于1个月 返回通讯录过期code
            if (STATUS_BPM_Y.equals(node.getNodeStatus()) && "4".equals(key) && ArrayUtils.contains(nodeIds,4) && now - node.getUpdateDate().getTime() > limit30) {
                result.setCode(BPM_UNDO_CODE);
                result.setInfo(BPM_UNDO_INFO);
                result.setData(key);
                return result;
            }

            //节点7 聚信立 认证状态为成功
            if (STATUS_BPM_Y.equals(node.getNodeStatus()) && "7".equals(key) && ArrayUtils.contains(nodeIds,8)) {
                //  检查认证时间 超过两个月要重新认证
                if (now - node.getUpdateDate().getTime() > limit60) {
                    result.setCode(BPM_UNDO_CODE);
                    result.setInfo(BPM_UNDO_INFO);
                    result.setData(key);
                    return result;
                }
            }

            if (STATUS_BPM_UP.equals(node.getNodeStatus())) {
                // 节点状态为提交中 还没拿到聚信立结果 将开关改为true
                haveNS004 = true;
            }

            //其余节点
            if (!ArrayUtils.contains(nodeIds, Integer.parseInt(key))) {
                continue;
            }
            Integer position = Arrays.binarySearch(nodeIds, Integer.parseInt(key));
            nodeIds = ArrayUtils.remove(nodeIds, position);
        }

        logger.info("当前per_id:" + per_id + ",当前数组元素:" + Arrays.toString(nodeIds));

        // 有节点未移除 还有未认证完成的节点 取第一个元素
        if (nodeIds.length > 0) {
            result.setCode(BPM_UNDO_CODE);
            result.setInfo(BPM_UNDO_INFO);
            result.setData(nodeIds[0]);
            return result;
        }

        // 聚信立没有结果 节点5为提交 其余为通过
        if (haveNS004) {
            result.setCode(BPM_UNDO_CODE);
            result.setInfo(BPM_UNDO_INFO);
            result.setData("0");
            return result;
        }
        // 聚信立有结果，所有节点通过，从数组中移除
        result.setCode(BPM_FINISH_CODE);
        result.setInfo(BPM_FINISH_INFO);
        return result;
    }

    /**
     * 增加调用第三方接口次数
     * @param per_id
     * @param type
     * @param count
     * @param status
     * @return
     */
    @Override
    public NoteResult addCount(String per_id, String type, String count,String status) {
        NoteResult result = new NoteResult(FAIL_CODE,FAIL_INFO);
        try {
            //插入数据
            Sensetime st = new Sensetime();
            st.setPerId(Integer.valueOf(per_id));
            st.setType(type);
            st.setCount(Integer.valueOf(count));
            st.setCreateTime(new Date());

            int i = sensetimeMapper.insert(st);
            if(i>0){//插入成功
                if("2".equals(type)||"4".equals(type)){//插入数据类型是人脸识别
                    if("s".equals(status)){//人脸识别结果为成功
                        //新增人脸识别认证成功接口
                        result = createBpmNode(per_id, NodeConstant.VERIFY_NODE_ID, STATUS_BPM_Y, "");
                        return result;
                    }
                    //人脸识别结果为失败
                    //查询该用户人脸识别的次数
                    int times = sensetimeMapper.selectTimes(per_id);
                    //3次，并且本次为失败，提交人脸识别拒绝节点
                    if(times>2 && "f".equals(status)){
                        NoteResult create = createBpmNode(per_id,NodeConstant.VERIFY_NODE_ID,STATUS_BPM_FAIL,"人脸识别3次未成功");
                        if(SUCCESS_CODE.equals(create.getCode())){//插入人脸识别拒绝节点成功
                            result.setCode(CodeReturn.VERIFY_FAIL_CODE);
                            result.setInfo("人脸识别失败超过3次，无法继续认证");
                        }
                    }else{
                        result.setCode(SUCCESS_CODE);
                        result.setInfo(SUCCESS_INFO);
                    }
                }else{//插入的数据不是人脸识别
                    result.setCode(SUCCESS_CODE);
                    result.setInfo(SUCCESS_INFO);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setInfo("系统错误");
            return result;
        }

        return result;
    }

    /**
     * 获取当前节点的认证状态
     * @param per_id
     * @param node_id
     * @return
     */
    @Override
    public NoteResult getNodeStatus(String per_id, String node_id) {

        NoteResult result = new NoteResult(FAIL_CODE,FAIL_INFO);
        // 新用户 没有bpm_id
        BpmNode node = bpmNodeMapper.selectByPerNode(Integer.valueOf(per_id), Integer.valueOf(node_id));
        if (node != null) {// 如果有node
            if (STATUS_BPM_UP.equals(node.getNodeStatus())) {
                result.setCode("207");
                result.setInfo("未认证");
                return result;
            }
            if (STATUS_BPM_Y.equals(node.getNodeStatus())) {
                result.setCode("208");
                result.setInfo("已认证");
                return result;
            }
            if (STATUS_BPM_FAIL.equals(node.getNodeStatus())) {
                result.setCode("209");
                result.setInfo("认证失败");
                return result;
            }
            if (STATUS_BPM_FAIL_B.equals(node.getNodeStatus())) {
                result.setCode("210");
                result.setInfo("认证失败，且为黑名单");
                return result;
            }
        } else {
            // node为空 表示超时已经删除手机和银行节点，返回211
            result.setCode("211");
            result.setInfo("由于系统原因，需要重新进行手机认证！");
            return result;
        }

        return result;
    }

    @Override
    public NoteResult zhima(String per_id) {
        NoteResult noteResult = new NoteResult(CodeReturn.FAIL_CODE,"系统繁忙,请稍后再试!");
        logger.info("开始进行芝麻:"+per_id+"的芝麻信用授权信息");
        /**
         * zhiMaCreditApi成功
         */
        String success = "1";
        String error = "0";
        try{
            /**
             * 查出身份证和姓名
             */
            logger.info("开始查询用户:"+per_id+"的身份证信息");
            Person p = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
            if(p.getCardNum() == null){
                noteResult.setCode("222");
                noteResult.setInfo("没有查询到身份证信息");
                return noteResult;
            }
            logger.info("开始查询用户:"+per_id+"的身份证信息,查询完毕");
            ReqData reqData = new ReqData();
            reqData.setName(p.getName());
            reqData.setIdNo(p.getCardNum());
            logger.info("开始查询用户:"+per_id+"的芝麻信用授权信息");
            ZhiMaCreditApiClient zhiMaCreditApiClient = new ZhiMaCreditApiClient();
            ZhiMaResult auth = zhiMaCreditApiClient.zhimaAuthInfoAuthquery(reqData);

            logger.info("返回的授权信息:"+JSONObject.toJSONString(auth));
            switch (auth.getCode()){
                case "0":
                    logger.info(per_id+"系统繁忙"+auth.getCode());
                    noteResult.setCode(CodeReturn.FAIL_CODE);
                    noteResult.setInfo("系统繁忙,请稍后重试!");
                    break;
                case "1":
                    String openId = auth.getResult().toString();

                    try {
                        //跑芝麻风控
                        logger.info(p.getId()+"-跑芝麻风控...");
                        RiskResultDto code = runZhimaRisk(p,Integer.valueOf(productId),zhiMaCreditApiClient);
                    }catch (Exception e){
                        e.printStackTrace();
                        logger.info(p.getId()+"-risk: 跑芝麻风控失败");
                    }

                    /**
                     * 已经授权过了 去查询分数
                     */
                    ZhiMa zhiMa = zhiMaMapper.selectByPer_Id(Integer.valueOf(per_id));
                    int sroce = 0;
                    if(zhiMa != null){
                        Date updateDate = zhiMa.getUpdateDate();
                        Date date = new Date();
                        long diffTime = date.getTime() - updateDate.getTime();
                        /**
                         * 芝麻分超过30天 则重新获取
                         */
                        long month = 2592000000L;
                        if(diffTime > month){
                            ZhiMaResult zhiMaSroce = zhiMaCreditApiClient.zhimaCreditScoreGet(openId);
                            if(error.equals(zhiMaSroce.getResult())){
                                return noteResult;
                            }
                            String sroceNow = zhiMaSroce.getResult().toString();
                            if(insertZhiMaSroce(per_id,sroceNow,auth.getMessage()) > 0){
                                sroce = Integer.valueOf(sroceNow);
                            }else{
                                return noteResult;
                            }
                        }else{
                            /**
                             *
                             * 芝麻分拉取 小于30天 不用重新拉取  节点的状态需要更新为通过
                             */
                            createBpmNode(per_id,NodeConstant.ZHIMA_NODE_ID,STATUS_BPM_Y,"");
                        }
                    }else{
                        logger.info(per_id+"db没数据,开始查询芝麻分数");
                        /**
                         * db没数据 查询一次
                         */
                        ZhiMaResult scoreGet = zhiMaCreditApiClient.zhimaCreditScoreGet(openId);
                        logger.info(per_id+"查询结果:"+JSONObject.toJSONString(scoreGet));
                        if(success.equals(scoreGet.getCode())){
                            if(insertZhiMaSroce(per_id,scoreGet.getResult().toString(),auth.getMessage()) > 0){
                                sroce = Integer.valueOf(scoreGet.getResult().toString());
                            }else{
                                return noteResult;
                            }
                        }
                    }

                    /**
                     * 分数大于 xxx即可通过
                     */
                    logger.info(per_id+"查询结果:通过");
                    noteResult.setCode(CodeReturn.SUCCESS_CODE);
                    noteResult.setInfo("下一节点");
                    break;
                case "2":
                    logger.info(per_id+"给用户生成授权URL");
                    ZhiMaAuthorizeDto zhiMaAuthorizeDto = new ZhiMaAuthorizeDto();
                    zhiMaAuthorizeDto.setAuthCode("M_APPSDK");
                    zhiMaAuthorizeDto.setChannelType("app");
                    zhiMaAuthorizeDto.setIdentityType("2");
                    zhiMaAuthorizeDto.setName(p.getName());
                    zhiMaAuthorizeDto.setIdNo(p.getCardNum());
                    ZhiMaResult result = zhiMaCreditApiClient.getZhiMaAuthorize(zhiMaAuthorizeDto);
                    logger.info(per_id+"生成结果:"+JSONObject.toJSONString(result));
                    if(error.equals(result.getCode())){
                        noteResult.setCode(CodeReturn.FAIL_CODE);
                        noteResult.setInfo("系统繁忙,请稍后重试!");
                        break;
                    }else if(success.equals(result.getCode())){
                        noteResult.setCode(ZHIMA_TOURL_CODE);
                        noteResult.setData(result.getResult()); //URL
                        noteResult.setInfo("提示用户去授权芝麻信用");
                        break;
                    }
                    break;
                case "3":
                    /**
                     * 直接拒绝
                     */
                    logger.info(per_id+"直接拒绝");
                    noteResult.setCode(ZHIMA_REFUSE_CODE);
                    break;
                case "4":
                    logger.info(per_id+"-该用户没有支付宝账户 或别的情况直接通过");
                    if(insertZhiMaSroce(per_id,"",auth.getMessage()) > 0){
                        noteResult.setCode(CodeReturn.SUCCESS_CODE);
                        noteResult.setInfo("下一节点");
                    }else{
                        return noteResult;
                    }
                    break;
                default:
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return noteResult;
    }

    /**
     * 跑芝麻风控
     * @param p  person
     * @param zhiMaCreditApiClient
     * @return
     */
    private RiskResultDto runZhimaRisk(Person p,int productId ,ZhiMaCreditApiClient zhiMaCreditApiClient) throws Exception{
        /**
         * 跑风控
         */
        ZhiMaRiskDto zhiMaRiskDto = new ZhiMaRiskDto();
        zhiMaRiskDto.setName(p.getName());
        zhiMaRiskDto.setPhone(p.getPhone());
        zhiMaRiskDto.setIdCard(p.getCardNum());
        zhiMaRiskDto.setRequestId("ZhiMaTest");
        FeatureDto featureDto = new FeatureDto();
        featureDto.setNodeCode(NodeConstant.ZHIMA_NODE); //节点名称 TODO
        featureDto.setWhiteTag(NodeConstant.WHITETAG);
        RiskResultDto riskResultDto = zhiMaCreditApiClient.runZhimaRisk(productId, zhiMaRiskDto, featureDto);
        return riskResultDto;
    }

    @Override
    public boolean isBlack(String phone,String idCard) {
        try {
            // 调用风控黑名单新接口，添加手机号码
            logger.info("调用风控黑名单接口参数，身份证号= 【" + idCard + "】, 手机号 = 【" + phone + "】");
            QueryResultDto queryResultDto = blacklistAPIClient.blacklistSingleQuery(idCard, phone);
            logger.info("调用风控黑名单接口返回，"+ JSONObject.toJSONString(queryResultDto));
            if ("1".equals(queryResultDto.getCode())){
                return true;
            }else {
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("调用风控黑名单接口失败，身份证号= 【" + idCard + "】, 手机号 = 【" + phone + "】");
            return true;
        }

    }

    private int insertZhiMaSroce(String per_id,String sroce,String description){
        ZhiMa zhiMa = new ZhiMa();
        zhiMa.setCreationDate(new Date());
        zhiMa.setPerId(Integer.valueOf(per_id));
        zhiMa.setZmScore(sroce);
        zhiMa.setUpdateDate(new Date());
        zhiMa.setDescription(description);
        createBpmNode(per_id,NodeConstant.ZHIMA_NODE_ID,STATUS_BPM_Y,"");
        return zhiMaMapper.insertSelective(zhiMa);
    }

    private int[] getBpmList(){

        String data;

        //缓存没有查询数据库
        if (StringUtils.isEmpty(jedisCluster.get(RedisConst.BPM_LIST_KEY))){
            data = codeValueMapper.getMeaningByTypeCode("bpm", "bpm_list");
            jedisCluster.set(RedisConst.BPM_LIST_KEY, data);
        }else{
            data = jedisCluster.get(RedisConst.BPM_LIST_KEY);
        }
        return ArrayUtils.toPrimitive(Arrays.stream(data.split(",")).map(Integer::valueOf).toArray(Integer[]::new));

    }


    public static void main(String[] args) {
        String nodes = "1,2,3,4";
        Integer[] integers = Arrays.stream(nodes.split(",")).map(Integer::valueOf).toArray(Integer[]::new);
        Arrays.stream(integers).forEach(System.out::println);


    }

}
