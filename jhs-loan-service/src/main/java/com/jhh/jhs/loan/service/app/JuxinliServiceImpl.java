package com.jhh.jhs.loan.service.app;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.app.JuxinliService;
import com.jhh.jhs.loan.api.app.LoanService;
import com.jhh.jhs.loan.api.app.RiskService;
import com.jhh.jhs.loan.common.util.*;
import com.jhh.jhs.loan.constant.NodeConstant;
import com.jhh.jhs.loan.entity.Juxinli.ReqDtoBasicInfo;
import com.jhh.jhs.loan.entity.enums.NodeStatusEnum;
import com.jinhuhang.risk.dto.FeatureDto;

import com.jhh.jhs.loan.entity.Juxinli.ReqDtoBasicInfoContact;
import com.jhh.jhs.loan.entity.app.*;
import com.jhh.jhs.loan.entity.enums.JuxinliEnum;

import com.jhh.jhs.loan.entity.manager_vo.ReqBackPhoneCheckVo;
import com.jhh.jhs.loan.mapper.app.*;
import com.jhh.jhs.loan.mapper.manager.ReviewMapper;
import com.jinhuhang.risk.service.impl.juxinli.JuxinliAPIClient;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by xuepengfei on 2017/10/20.
 */
@Service
public class JuxinliServiceImpl implements JuxinliService {

    private static Logger log = Logger.getLogger(JuxinliServiceImpl.class);

    @Autowired
    private LoanService loanService;
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private PrivateMapper privateMapper;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private BorrowListMapper borrowListMapper;
    @Autowired
    private JedisCluster jedisCluster;
    @Autowired
    private RiskService riskService;
    @Autowired
    private BpmNodeMapper bpmNodeMapper;


    //聚信立回调地址
    @Value("${productId}")
    private String productId;
    @Value("${jxlCallbackUrl}")
    private String jxlCallbackUrl;

    @Override
    public NoteResult backPhoneCheckMessage(ReqBackPhoneCheckVo callback) {
        //风控结果状态
        NoteResult result = new NoteResult(JuxinliEnum.JXL_ERROR.getCode(),"失败");
        try {
            Person person = personMapper.getPersonByPhone(callback.getPhone());
            Integer per_id = person.getId();
            BpmNode juxinliNode = bpmNodeMapper.selectByPerNode(per_id, NodeConstant.JXL_NODE_ID);

            //两种情况不允许再回调 2018.3.14 再加一种 该节点已经为认证通过了 直接返回成功
            if (juxinliNode != null && NodeStatusEnum.DONE_IDENTIFICATION.getCode().equals(juxinliNode.getNodeStatus())){
                log.info(String.format("--backPhoneCheckMessage-->【%s】聚信立节点状态为【%s】,聚信立回调重复", per_id, juxinliNode.getNodeStatus()));
                //该节点已经为认证通过了 直接返回成功
                result.setCode(JuxinliEnum.JXL_SUCCESS.getCode());
                result.setInfo("回调重复,节点状态已经为认证成功");
                return result;
            }
            //1.借款状态不为申请中或者已取消，返回回调失败
            BorrowList borrowList = borrowListMapper.selectNow(per_id);
            log.info(String.format("--backPhoneCheckMessage-->【%s】当前借款状态为【%s】", per_id, borrowList.getBorrStatus()));
            if (!(CodeReturn.STATUS_APLLY.equals(borrowList.getBorrStatus()) || CodeReturn.STATUS_CANCEL.equals(borrowList.getBorrStatus()))){
                result.setCode(JuxinliEnum.JXL_SUCCESS.getCode());
                result.setInfo("借款状态有误，回调失败");
                return result;
            }
            /**
             * 注释掉此部分代码
             */
            //2.requestId 不是该用户当前的聚信立token，返回回调失败
//            String juxinliToken = jedisCluster.get(RedisConst.JUXINLI_TOKEN + per_id);
//            if (StringUtils.isEmpty(juxinliToken) || !callback.getRequestId().equals(juxinliToken)){
//                //token为空或者 传过来的requestId 不为当前的token
//                result.setCode(JuxinliEnum.JXL_SUCCESS.getCode());
//                result.setInfo("token错误");
//                return result;
//            }
            //没有返回回调失败 继续走流程
            // 通过
            if (JuxinliEnum.JXL_SUCCESS.getCode().equals(callback.getNode_status())) {
                //解除聚信立5条风控规则 ，
                //   isManual 1审核 2解除   type 1通讯录 2聚信立
                loanService.manuallyReview(per_id.toString(), 2, 2, callback.getDescription());
                riskService.createBpmNode(per_id.toString(), NodeConstant.JXL_NODE_ID, CodeReturn.STATUS_BPM_Y, callback.getDescription());
            }
            if (JuxinliEnum.JXL_REFUSE.getCode().equals(callback.getNode_status())) {
                // 认证失败 把手机节点改为NS003 借款状态改为审核未通过
                riskService.createBpmNode(per_id.toString(), NodeConstant.JXL_NODE_ID, CodeReturn.STATUS_BPM_FAIL, callback.getDescription());
                borrowList.setBorrStatus(CodeReturn.STATUS_REVIEW_FAIL);
                if(borrowListMapper.updateByPrimaryKeySelective(borrowList)<1){
                    log.info(String.format("--backPhoneCheckMessage-->【%s】更新借款【%s】状态为【%s】,执行失败", per_id, borrowList.getId(), CodeReturn.STATUS_REVIEW_FAIL));
                    //借款状态没有更新成功 返回失败
                    result.setInfo("借款状态没有更新成功，回调失败");
                    return result;
                }
            }

            if (JuxinliEnum.JXL_MENUAL.getCode().equals(callback.getNode_status())) {
                //触碰聚信立5条风控规则 ，增加人工审核.相当于通过
                //             isManual 1审核 2解除   type 1通讯录 2聚信立
                loanService.manuallyReview(per_id.toString(), 2, 1, callback.getDescription());
                riskService.createBpmNode(per_id.toString(), NodeConstant.JXL_NODE_ID, CodeReturn.STATUS_BPM_Y, callback.getDescription());
            }
            if (JuxinliEnum.JXL_ERROR.getCode().equals(callback.getNode_status())) {
                //异常 ，改回到NS001
                //             isManual 1审核 2解除   type 1通讯录 2聚信立
                riskService.createBpmNode(per_id.toString(), NodeConstant.JXL_NODE_ID, CodeReturn.STATUS_BPM_N, callback.getDescription());
            }
            result.setCode(JuxinliEnum.JXL_SUCCESS.getCode());
            result.setInfo("成功");

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public NoteResult risk(ReqDtoBasicInfo reqDtoBasicInfo) {
        NoteResult result = new NoteResult();

        //根据per_id查询个人信息
        String per_id = reqDtoBasicInfo.getPer_id();
        Person person = personMapper.selectByPrimaryKey(Integer.valueOf(per_id));
        if (person == null) {
            log.info(String.format("聚信立认证异常：未查询到对应的个人信息【%s】...", per_id));
            result.setInfo("用户不存在");
            return result;
        }

        String requestId, password;
        // command_code为空  第一步，上传服务密码
        if (StringUtils.isEmpty(reqDtoBasicInfo.getCode())) {
            //存服务密码
            password = reqDtoBasicInfo.getPassword();
            person.setPhoneService(Base64.encode(password));
            personMapper.updateByPrimaryKeySelective(person);
            // 生成redis key  存入requestId
            requestId = BorrNum_util.createBorrNum();
            jedisCluster.set(RedisConst.JUXINLI_TOKEN + per_id, requestId);

            // 修改redis过期时间为24小时
            jedisCluster.expire(RedisConst.JUXINLI_TOKEN + per_id, 24 * 60 * 60);
//            jedisCluster.expire(RedisConst.JUXINLI_TOKEN + per_id, 24 * 60 * 60 * 1000);
        } else {
            //command_code不为空 不是第一步 取服务密码
            password = Base64.decode(person.getPhoneService());
            //如果redis key 失效了 返回重新认证
            if (StringUtils.isEmpty(jedisCluster.get(RedisConst.JUXINLI_TOKEN + per_id))) {
                result.setInfo("验证超时，请返回重新认证");
                return result;
            }
            //redis key 没有失效
            requestId = jedisCluster.get(RedisConst.JUXINLI_TOKEN + per_id);
        }

        log.info("聚信立认证请求参数封装-----------------------begin---------------------------");
        //封装dubbo接口的第一个参数
        reqDtoBasicInfo.setRequestId(requestId);
        reqDtoBasicInfo.setPassword(password);
        reqDtoBasicInfo.setName(person.getName());
        reqDtoBasicInfo.setId_card_num(person.getCardNum());
        reqDtoBasicInfo.setCell_phone_num(person.getPhone());
        reqDtoBasicInfo.setRelation_contacts_url(person.getContactUrl());

        Private info = privateMapper.selectByPerId(Integer.valueOf(per_id));
        reqDtoBasicInfo.setHome_addr(info.getUsuallyaddress());
        reqDtoBasicInfo.setWork_addr(info.getBusiAddress());
        reqDtoBasicInfo.setWork_tel(info.getBusiPhone());

        //封装2个联系人信息
        List<ReqDtoBasicInfoContact> list = new ArrayList<>();

        //联系人--亲属
        ReqDtoBasicInfoContact relatives = new ReqDtoBasicInfoContact();
        //亲属联系电话
        relatives.setContact_tel(info.getRelaPhone());
        //亲属姓名
        relatives.setContact_name(info.getRelativesName());
        //亲属关系（0.1.2.3）
        relatives.setContact_type(info.getRelatives());
        list.add(relatives);

        //联系人--社会关系
        ReqDtoBasicInfoContact society = new ReqDtoBasicInfoContact();
        //社会关系联系电话
        society.setContact_tel(info.getSociPhone());
        //社会关系姓名
        society.setContact_name(info.getSocietyName());
        //社会关系（4.5.6）
        society.setContact_type(info.getSociety());
        list.add(society);

        reqDtoBasicInfo.setBaseInfoContacts(list);

        //封装dubbo接口方法的第二个参数 回调地址
        FeatureDto featureDto = new FeatureDto();
        featureDto.setCallbackURI(jxlCallbackUrl);
        featureDto.setNodeCode(NodeConstant.JXL_NODE);
        featureDto.setOrgCodeList("jxl");
        log.info("聚信立认证请求参数封装-----------------------end---------------------------");

        String requestResult;
        try {
            com.jinhuhang.risk.dto.jxl.jsonbean.ReqDtoBasicInfo riskReqDtoBasicInfo = new com.jinhuhang.risk.dto.jxl.jsonbean.ReqDtoBasicInfo();
            BeanUtils.copyProperties(reqDtoBasicInfo, riskReqDtoBasicInfo);
            log.info(String.format("聚信立请求参数：%s, %s, %s", productId, riskReqDtoBasicInfo, featureDto));
            log.info(String.format("聚信立请求参数：jsonStr【%s】", JSONObject.toJSONString(riskReqDtoBasicInfo)));
            log.info(String.format("聚信立请求参数：jsonFeature【%s】", JSONObject.toJSONString(featureDto)));

            requestResult = new JuxinliAPIClient().runRequestReport(Integer.valueOf(productId), riskReqDtoBasicInfo, featureDto);
            log.info(String.format("聚信立返回参数：%s", requestResult));
        } catch (Exception e) {
            log.info(String.format("聚信立请求异常：%s", e.getMessage()));
            e.printStackTrace();
            result.setCode(JuxinliEnum.JXL_ERROR.getCode());
            result.setInfo("请求超时，请稍后重新认证");
            return result;
        }

        String code = JSONObject.parseObject(requestResult).getString("code");
        String msg = JSONObject.parseObject(requestResult).getString("msg");

        //根据请求结果 APP后台改变用户节点的状态及人工审核状态等。
        if (JuxinliEnum.JXL_SUCCESS.getCode().equals(code)) {
            //0000：成功	状态为NS002	跳转下一认证节点
            riskService.createBpmNode(per_id, NodeConstant.JXL_NODE_ID, CodeReturn.STATUS_BPM_Y, msg);
            //过人工审核模型
            //   isManual 1审核 2解除   type 1通讯录 2聚信立
            loanService.manuallyReview(per_id, 2, 2, msg);
            result.setCode(code);
            result.setInfo(msg);
            return result;
        }

        if (JuxinliEnum.JXL_REFUSE.getCode().equals(code)) {
            //1000：失败	状态为NS003	拒绝，提示信用等级过低
            riskService.createBpmNode(per_id, NodeConstant.JXL_NODE_ID, CodeReturn.STATUS_BPM_FAIL, msg);
            //更改借款状态为审核未通过
            BorrowList borrowList = borrowListMapper.selectNow((Integer.valueOf(per_id)));
            borrowList.setBorrStatus(CodeReturn.STATUS_REVIEW_FAIL);
            if (borrowListMapper.updateByPrimaryKeySelective(borrowList)<1){
                result.setInfo("数据更新失败");
                return result;
            }
            result.setCode(code);
            result.setInfo(msg);
            return result;
        }

        if (JuxinliEnum.JXL_ERROR.getCode().equals(code)) {
            //2000：异常-- 展示后台信息	保持原来的状态 NS001
            riskService.createBpmNode(per_id, NodeConstant.JXL_NODE_ID, CodeReturn.STATUS_BPM_N, msg);
            result.setCode(code);
            result.setInfo(msg);
            return result;
        }

        if (JuxinliEnum.JXL_MENUAL.getCode().equals(code)) {
            //8888：人工审核	状态为NS002	跳转下一认证节点
            riskService.createBpmNode(per_id, NodeConstant.JXL_NODE_ID, CodeReturn.STATUS_BPM_Y, msg);
            //过人工审核模型
            //   isManual 1审核 2解除   type 1通讯录 2聚信立
            loanService.manuallyReview(per_id, 2, 1, msg);
            result.setCode(code);
            result.setInfo(msg);
            return result;
        }

        if (JuxinliEnum.JXL_COLLECTING.getCode().equals(code)) {
            //10008：已受理采集	状态为NS004	跳转下一认证节点
            riskService.createBpmNode(per_id, NodeConstant.JXL_NODE_ID, CodeReturn.STATUS_BPM_UP, msg);
            result.setCode(code);
            result.setInfo(msg);
            return result;
        }

        //其余（10002,10003,10001,10022） 状态不变 NS001 传code给APP
        riskService.createBpmNode(per_id, NodeConstant.JXL_NODE_ID, CodeReturn.STATUS_BPM_N, msg);
        result.setCode(code);
        result.setInfo(msg);
        return result;
    }
}
