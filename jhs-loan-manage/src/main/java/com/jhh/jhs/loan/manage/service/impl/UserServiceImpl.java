package com.jhh.jhs.loan.manage.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.entity.common.ResponseCode;
import com.jhh.jhs.loan.entity.loan.Collectors;
import com.jhh.jhs.loan.entity.manager_vo.CardPicInfoVo;
import com.jhh.jhs.loan.entity.manager_vo.PrivateVo;
import com.jhh.jhs.loan.manage.entity.Response;
import com.jhh.jhs.loan.manage.entity.RiskBlacklistInfoLog;
import com.jhh.jhs.loan.manage.forkjoin.ForkJoinTask;
import com.jhh.jhs.loan.manage.mapper.CollectorsMapper;
import com.jhh.jhs.loan.manage.mapper.OrderMapper;
import com.jhh.jhs.loan.manage.mapper.PersonMapper;
import com.jhh.jhs.loan.manage.service.user.UserService;
import com.jhh.jhs.loan.manage.utils.Detect;
import com.jinhuhang.risk.dto.QueryResultDto;
import com.jinhuhang.risk.dto.blacklist.jsonbean.BlackListDto;
import com.jinhuhang.risk.service.impl.blacklist.BlacklistAPIClient;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * 用户相关
 *
 * @author
 */
@Service
@Setter
@Log4j
public class UserServiceImpl implements UserService {

    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CollectorsMapper collectorsMapper;

    @Autowired
    private BlacklistAPIClient riskClient;

    private static final String RISK_SUCCESS = "1";

    @Override
    public Response getUserInfo(Integer perId) {
        Response response = new Response().code(ResponseCode.FIAL);
        PrivateVo privateVo = personMapper.queryUserInfo(perId);
        if (privateVo != null) {
            try {
                if (privateVo.getImageZ() != null) {
                    String path = privateVo.getImageZ();
                    if (path != null) {
                        privateVo.setImageZ("/loan-manage/proxy/image.action?path=" + URLEncoder.encode(path, "UTF-8"));
                    }
                }
                if (privateVo.getImageF() != null) {
                    String path = privateVo.getImageF();
                    if (path != null) {
                        privateVo.setImageF("/loan-manage/proxy/image.action?path=" + URLEncoder.encode(path, "UTF-8"));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                response.data(privateVo).msg("fail").code(ResponseCode.FIAL);
            }

            setBlackList(privateVo);
            response.data(privateVo).msg("success").code(ResponseCode.SUCCESS);
        }
        return response;
    }

    @Override
    public Response getIdentityCard(Integer perId) {
        Response response = new Response().code(ResponseCode.FIAL);
        CardPicInfoVo cardPicInfoVo = personMapper.getCardPicById(perId);
        if (cardPicInfoVo != null) {
            try {
                if (cardPicInfoVo.getImageZ() == null && cardPicInfoVo.getImageUrlZ() != null) {
                    String path = cardPicInfoVo.getImageUrlZ();
                    if (path != null) {
                        cardPicInfoVo.setImageZ("/loan-manage/proxy/image.action?path=" + URLEncoder.encode(path, "UTF-8"));
                    }
                }
                if (cardPicInfoVo.getImageF() == null && cardPicInfoVo.getImageUrlF() != null) {
                    String path = cardPicInfoVo.getImageUrlF();
                    if (path != null) {
                        cardPicInfoVo.setImageF("/loan-manage/proxy/image.action?path=" + URLEncoder.encode(path, "UTF-8"));
                    }
                }
                response.data(cardPicInfoVo).msg("success").code(ResponseCode.SUCCESS);
            } catch (UnsupportedEncodingException e) {
                response.data(cardPicInfoVo).msg("fail").code(ResponseCode.FIAL);
            }
        }
        return response;
    }

    @Override
    public String getNameByPersonId(Integer personId) {
        if (Detect.isPositive(personId)) {
            Person person = personMapper.selectByPrimaryKey(personId);
            return person != null ? person.getName() : "";
        }
        return "";
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = java.lang.Exception.class)
    public Response userBlockWhite(Integer personId, String operatorNum, String operator, String reason, Integer type) throws Exception {
        Response response = new Response().code(ResponseCode.FIAL).msg("操作失败");
        if (!Detect.isPositive(personId)) {
            response.code(ResponseCode.FIAL).msg("拉黑用户不能为空");
            return response;
        }
        if (type < 0) {
            response.code(ResponseCode.FIAL).msg("操作类型为空");
            return response;
        }
        if (reason.length() > 250) {
            response.code(ResponseCode.FIAL).msg("操作理由超过字数限制(不能超过250个字符)");
            return response;
        }

        Person person = personMapper.selectByPrimaryKey(personId);
        if (!Detect.notEmpty(operator)) {
            Collectors collectors = new Collectors();
            collectors.setUserSysno(operatorNum);
            collectors = collectorsMapper.selectOne(collectors);
            operator = collectors.getUserName();
        }

        BlackListDto blackListDto = new BlackListDto();
        blackListDto.setCreateTime(Calendar.getInstance().getTime());
        blackListDto.setReason(reason);
        blackListDto.setType(type);
        blackListDto.setHandlerName(operator);
        blackListDto.setHandlerNo(operatorNum);
        blackListDto.setIdcard(person.getCardNum());
        blackListDto.setPhone(person.getPhone());
        blackListDto.setName(person.getName());
        blackListDto.setSys("slow_ecovery");

        log.info(String.format("------->调用风控拉黑/洗白接口 入参【%s】", JSON.toJSONString(blackListDto)));
        QueryResultDto queryResultDto = riskClient.blacklist(blackListDto);
        log.info(String.format("------->调用风控拉黑/洗白接口 返回【%s】", JSON.toJSONString(queryResultDto)));

        if (queryResultDto == null) {
            return response;
        }

        if (RISK_SUCCESS.equals(queryResultDto.getCode())) {
            response.code(ResponseCode.SUCCESS).msg("操作成功");
        }

        return response;
    }

    @Override
    public Response getBlackList(Integer personId) {

        if (!Detect.isPositive(personId)) {
            return new Response().code(ResponseCode.SUCCESS).data(null);
        }

        Person person = personMapper.selectByPrimaryKey(personId);

        if (ObjectUtils.isEmpty(person)){
            return new Response().code(ResponseCode.FIAL).msg("用户不存在").data(null);
        }

        // 调用风控黑名单新接口，添加手机号码
        QueryResultDto result = null;
        try {
            result = riskClient.blacklistLogQueryByIdCard(person.getCardNum());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用风控黑名单接口失败,身份证号码 = "+ person.getCardNum() + ",手机号码= " + person.getPhone());
        }

        // 调用风控黑名单接口失败|或者返回的结果为空，这里做返回
        if (ObjectUtils.isEmpty(result)) {
            return new Response().code(ResponseCode.SUCCESS).data(null);
        }

        if (!RISK_SUCCESS.equals(result.getCode())) {
            return new Response().code(ResponseCode.SUCCESS).data(null);
        }

        List<RiskBlacklistInfoLog> list = (List) result.getModel();
        return new Response().code(ResponseCode.SUCCESS).data(list);
    }

    @Override
    public Response getBankList(Integer personId) {
        List bank = null;
        if (Detect.isPositive(personId)) {
            bank = personMapper.getBankByPerId(personId);
        }

        return new Response().code(200).data(bank);
    }

    @Override
    public Response getOrderList(Integer personId) {
        Map<String, Object> args = new HashMap<String, Object>();
        Integer[] types = new Integer[]{1, 2, 4, 5, 6, 7, 8, 11, 12, 13, 14, 15, 16, 17, 18};
        args.put("perId", personId);
        args.put("type_s", types);
        List orders = orderMapper.getOrdersByArgs(args);
        return new Response().code(200).data(orders);
    }

    @Override
    public Response getNodeList(Integer personId) {
        List nodeList = null;
        if (Detect.isPositive(personId)) {
            nodeList = personMapper.getNodeByPerId(Integer.toString(personId));

        }
        return new Response().code(200).data(nodeList);
    }

    @Override
    public Response getNodeDetailList(Integer personId) {
        Map map = new HashMap();
        if (Detect.isPositive(personId)) {
            Person person = personMapper.selectByPrimaryKey(personId);
            if (person != null) {
                //未过期的节点详情数据
                map.put("idCard", person.getCardNum());
                map.put("expires", "2");
                List notExpires = personMapper.getNodeDetailByPerId(map);
                //过期的节点详情数据
                map.put("expires", "1");
                List isExpires = personMapper.getNodeDetailByPerId(map);
                map.put("notExpires", notExpires);
                map.put("isExpires", isExpires);
            }

        }
        return new Response().code(200).data(map);
    }

    /**
     * 查询用户列表
     */
    @Override
    public Response getUsers(Map<String, String[]> args) {
        List<Map> list = personMapper.getUsers(getargs(args));
        return new Response().code(200).data(handlerUserBlackList(list));
    }
    /**
     * 查询渠道用户列表
     */
    @Override
    public Response getChannelUsers(Map<String, String[]> args) {
        List<Map> list = personMapper.getChannelUsers(getChannelArgs(args));
        return new Response().code(200).data(handlerUserBlackList(list));
    }

    @Override
    public List getSource(String code) {
        return personMapper.getRegisterSource(code);
    }

    /**
     * 组装查询条件: 过滤条件 filter，排序条件 sort
     */
    private Map getargs(Map<String, String[]> args) {
        Iterator<String> keys = args.keySet().iterator();
        Map arg = new HashMap();
        while (keys.hasNext()) {
            String key = keys.next();
            if ("filter".equals(key)) {
                String[] filter = args.get(key);
                if (filter.length > 0 && StringUtils.isNotEmpty(filter[0])) {
                    String st = filter[0];
                    JSONArray js = JSON.parseArray(st);
                    for (int i = 0; i < js.size(); i++) {
                        if (!"and".equals(js.get(i).toString())) {
                            if (js.get(i) instanceof JSONArray) {
                                JSONArray jss = JSON.parseArray(js.get(i)
                                        .toString());
                                if (jss.get(0) instanceof JSONArray) {
                                    JSONArray jsdate = (JSONArray) jss;
                                    for (int j = 0; j < jsdate.size(); j++) {
                                        setDate(arg, jsdate.get(j));
                                    }
                                } else {
                                    Object o = jss.get(2);
                                    if (o instanceof JSONObject) {
                                        arg.put(jss.get(0),
                                                ((JSONObject) o).get("value"));
                                    } else {
                                        setDate(arg, jss);
                                        arg.put(jss.get(0), jss.get(2));
                                    }
                                }

                            } else {
                                Object o = js.get(2);
                                if (o instanceof JSONObject) {
                                    arg.put(js.get(0),
                                            ((JSONObject) o).get("value"));
                                } else {
                                    arg.put(js.get(0), js.get(2));
                                }
                                break;
                            }

                        }
                    }

                }
            } else if ("sort".equals(key)) {
                String[] sort = args.get(key);
                if (sort.length > 0 && StringUtils.isNotEmpty(sort[0])) {
                    JSONObject jo = JSON.parseArray(sort[0]).getJSONObject(0);
                    arg.put("selector", jo.get("selector"));
                    arg.put("desc", jo.get("desc"));
                }
            }

        }
        return arg;
    }

    private Map getChannelArgs(Map<String, String[]> args) {
        Iterator<String> keys = args.keySet().iterator();
        Map arg = new HashMap();
        while (keys.hasNext()) {
            String key = keys.next();
            if ("filter".equals(key)) {
                String[] filter = args.get(key);
                if (filter.length > 0 && StringUtils.isNotEmpty(filter[0])) {
                    String st = filter[0];
                    JSONArray js = JSON.parseArray(st);
                    for (int i = 0; i < js.size(); i++) {
                        if (!"and".equals(js.get(i).toString())) {
                            if (js.get(i) instanceof JSONArray) {
                                JSONArray jss = JSON.parseArray(js.get(i)
                                        .toString());
                                if (jss.get(0) instanceof JSONArray) {
                                    JSONArray jsdate = (JSONArray) jss;
                                    for (int j = 0; j < jsdate.size(); j++) {
                                        setDate(arg, jsdate.get(j));
                                    }
                                } else {
                                    Object o = jss.get(2);
                                    if (o instanceof JSONObject) {
                                        arg.put(jss.get(0),
                                                ((JSONObject) o).get("value"));
                                    } else {
                                        setDate(arg, jss);
                                        arg.put(jss.get(0), jss.get(2));
                                    }
                                }

                            } else {
                                Object o = js.get(2);
                                if (o instanceof JSONObject) {
                                    arg.put(js.get(0),
                                            ((JSONObject) o).get("value"));
                                } else {
                                    arg.put(js.get(0), js.get(2));
                                }
                                break;
                            }

                        }
                    }

                }
            } else if ("sort".equals(key)) {
                String[] sort = args.get(key);
                if (sort.length > 0 && StringUtils.isNotEmpty(sort[0])) {
                    JSONObject jo = JSON.parseArray(sort[0]).getJSONObject(0);
                    arg.put("selector", jo.get("selector"));
                    arg.put("desc", jo.get("desc"));
                }
            }else if ("source".equals(key)) {
                String sourceArr[] = args.get(key);
                if (sourceArr.length > 0 && StringUtils.isNotEmpty(sourceArr[0])) {
                    /*JSONObject jsonObject = JSON.parseArray(sourceArr[0]).getJSONObject(0);
                    arg.put("source", jsonObject.get("source"));*/
                    arg.put("source",sourceArr[0].toString());
                }
            }

        }
        return arg;
    }

    private void setDate(Map<String, Object> arg, Object js) {

        if (js instanceof JSONArray) {
            JSONArray jss = (JSONArray) js;
            if (jss.get(1).toString().indexOf(">") > -1) {
                arg.put(jss.getString(0) + "_start", jss.getString(2));
            } else if (jss.get(1).toString().indexOf("<") > -1) {
                arg.put(jss.getString(0) + "_end", jss.getString(2));
            }
        } else {

        }

    }

    /**
     * 根据身份证号码查询该用户是否在黑名单
     *
     * @param privateVo
     */
    private void setBlackList(PrivateVo privateVo) {
        if (StringUtils.isEmpty(privateVo.getCardNum())) {
            return;
        }

        if (ObjectUtils.isEmpty(privateVo)){
            return;
        }

        try {

            // 调用风控黑名单新接口，添加手机号码
            QueryResultDto result = riskClient.blacklistSingleQuery(privateVo.getCardNum(),privateVo.getPhone());

            if (ObjectUtils.isEmpty(result)) {
                return;
            }

            if ("0".equals(result.getCode())) {
                privateVo.setBlacklist("Y");
            } else {
                privateVo.setBlacklist("N");
            }
        } catch (Exception e) {
            log.error("======调用风控查询黑名单失败===========");
            log.error(e);
        }

    }

    /**
     * 批量查询黑名单列表数据
     *
     * @param list
     */
    private List handlerUserBlackList(List<Map> list) {
        //  入参为空，则返回 2018-4-13 风控黑名单新接口
        if(CollectionUtils.isEmpty(list)){
            return list;
        }

        List<BlackListDto> blackListDtos = Lists.newArrayList();

        list.forEach(map -> {
            BlackListDto dto = new BlackListDto();
            // 此处不能合并，防止ClassCastException
            if(!ObjectUtils.isEmpty(map.get("card_num"))){
                dto.setIdcard(map.get("card_num").toString());
            }

            if(!ObjectUtils.isEmpty(map.get("phone"))){
                dto.setPhone(map.get("phone").toString());
            }
            if(StringUtils.isNotBlank(dto.getPhone())||StringUtils.isNotBlank(dto.getIdcard())) {
                blackListDtos.add(dto);
            }
        });

        // 风控黑名单手机号或者身份证号为空，则返回
        if(CollectionUtils.isEmpty(blackListDtos)){
            return list;
        }

        // 用ForkJoinTask 批量查询黑名单，每个任务大小为20000
        List blackList = ForkJoinPool.commonPool().invoke(new ForkJoinTask(blackListDtos, 0, blackListDtos.size(), 20000));

        return getList(list, blackList);
    }

    private static List<Map> getList(List<Map> list, List blackList) {
        list.forEach(map -> {
            if (blackList.contains(map.get("card_num"))||blackList.contains(map.get("phone"))) {
                map.put("blacklist", "Y");
            } else {
                map.put("blacklist", "N");
            }
        });
        return list;
    }

}
