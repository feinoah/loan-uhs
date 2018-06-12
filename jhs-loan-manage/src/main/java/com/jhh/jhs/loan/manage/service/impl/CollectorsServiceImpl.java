package com.jhh.jhs.loan.manage.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jhh.jhs.loan.api.constant.StateCode;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.sms.SmsService;
import com.jhh.jhs.loan.common.enums.SmsTemplateEnum;
import com.jhh.jhs.loan.common.util.EmaySmsUtil;
import com.jhh.jhs.loan.entity.common.Constants;
import com.jhh.jhs.loan.entity.enums.DclTypeEnum;
import com.jhh.jhs.loan.entity.loan.Collectors;
import com.jhh.jhs.loan.entity.manager.LoanCompany;
import com.jhh.jhs.loan.entity.utils.ManagerResult;
import com.jhh.jhs.loan.manage.entity.Result;
import com.jhh.jhs.loan.manage.mapper.CollectorsMapper;
import com.jhh.jhs.loan.manage.mapper.LoanCompanyMapper;
import com.jhh.jhs.loan.manage.mapper.ReviewMapper;
import com.jhh.jhs.loan.manage.service.loan.CollectorsService;
import com.jhh.jhs.loan.manage.utils.AuthUtil;
import com.jhh.jhs.loan.manage.utils.Detect;
import com.jhh.jhs.loan.manage.utils.MD5Util;
import com.jhh.jhs.loan.manage.utils.UrlReader;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.JedisCluster;
import tk.mybatis.mapper.MapperException;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
@Log4j
public class CollectorsServiceImpl implements CollectorsService {
    @Autowired
    private JedisCluster jedisCluster;
    @Autowired
    private LoanCompanyMapper companyMapper;
    @Autowired
    private CollectorsMapper collectorsMapper;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private SmsService smsService;

    @Override
    public PageInfo<Collectors> selectCollectors(Map<String, Object> queryMap, int offset, int size, String userNo) {

        Collectors queryCollectors = new Collectors();
        queryCollectors.setUserSysno(userNo);
        Collectors Collectors = collectorsMapper.selectOne(queryCollectors);


        Example queryExample = new Example(Collectors.class);
        Example.Criteria criteria = queryExample.createCriteria();
        //查询当前查询人所属公司,是全局管理员时,可查看所有人,贷后部可以查看除风控部、系统管理、运营管理部所有人员，否则仅能查看自己公司下的人
        if (!Integer.valueOf(UrlReader.read("auth.system.super")).equals(Collectors.getLevelType())) {
            //查询所有的公司
            List<LoanCompany> companies = companyMapper.selectAll();
            Set<Integer> companiesInSet = new HashSet();
            for (LoanCompany company : companies) {
                if ("金互行-客服部".equals(company.getName()) || "金互行-风控部".equals(company.getName()) || "金互行-系统管理".equals(company.getName()) || "金互行-运营管理部".equals(company.getName())) {
                    companiesInSet.add(company.getId());
                }
            }
            if (Collectors.getLevelType() == 1) {
                criteria.andNotIn("userGroupId", companiesInSet);
            } else {
                criteria.andEqualTo("userGroupId", Collectors.getUserGroupId());
            }
        }
        for (String entry : queryMap.keySet()) {
            try {
                criteria.andEqualTo(entry, queryMap.get(entry));
            } catch (MapperException e) {
                log.warn(e);
            }
        }

        if (queryMap.containsKey("selector") && queryMap.containsKey("desc")) {
            Example.OrderBy orderBy = queryExample.orderBy(queryMap.get("selector").toString());
            if ("desc".equals(queryMap.get("desc"))) {
                orderBy.desc();
            }
            orderBy.orderBy("status").orderBy("createDate").desc();
        } else {
            queryExample.orderBy("status").orderBy("createDate").desc();
        }

        PageHelper.offsetPage(offset, size);
        List<Collectors> Collectorss = collectorsMapper.selectByExample(queryExample);
        PageInfo<Collectors> pageInfo = new PageInfo<>(Collectorss);

        return pageInfo;
    }

    @Override
    public String generateNewSysno() {
        int userNo = collectorsMapper.selectMaxId() + 1;
        return "DS" + userNo;
    }

    @Override
    public int editCollectors(Collectors Collectors) {
        int opCount = 0;

        Example companyExample = new Example(LoanCompany.class);
        companyExample.createCriteria().andEqualTo("name", "金互行-风控部");
        List<LoanCompany> companies = companyMapper.selectByExample(companyExample);

        if (Collectors.getId() == null) {

            Collectors.setPassword(MD5Util.encryptString("123456"));//初始化密码
            Collectors.setCreateDate(new Date());
            Collectors.setCreateUser("系统");
            Collectors.setUpdateDate(new Date());
            opCount = collectorsMapper.insertSelective(Collectors);
            if (opCount > 0) {
//                CollectorsBack back = new CollectorsBack();
//                back.setUserSysno(Collectors.getUserSysno());
//                back.setUserName(Collectors.getUserName());
//                CollectorsBackMapper.insert(back);
                //如果是风控部人员
                //不再关联拉黑
                /*if (companies != null && companies.size() > 0 && companies.get(0).getId() == Collectors.getUserGroupId()) {
                    Riewer riewer = new Riewer();
                    riewer.setEmplloyeeName(Collectors.getUserName());
                    riewer.setEmployNum(Collectors.getUserSysno());
                    riewer.setStatus("A".equals(Collectors.getStatus()) ? "y" : "n");
                    riewer.setCreationDate(new Date());
                    riewerMapper.insert(riewer);
                }*/
            }
        } else {
            Example example = new Example(Collectors.class);
            example.createCriteria().andEqualTo("id", Collectors.getId());
            opCount = collectorsMapper.updateByExampleSelective(Collectors, example);
            /*if (opCount > 0) {
                //如果是风控部人员
                if (companies != null && companies.size() > 0 && companies.get(0).getId() == Collectors.getUserGroupId()) {
                    Riewer riewer = new Riewer();
                    riewer.setEmplloyeeName(Collectors.getUserName());
                    riewer.setEmployNum(Collectors.getUserSysno());
                    riewer.setStatus("A".equals(Collectors.getStatus()) ? "y" : "n");
                    riewer.setCreationDate(new Date());

                    Example updateExample = new Example(Riewer.class);
                    updateExample.createCriteria().andEqualTo("employNum", Collectors.getUserSysno());

                    int count = riewerMapper.updateByExample(riewer, updateExample);
                    System.out.println("更新条数:" + count);
                }
            }*/
        }

        return opCount;
    }

    @Override
    public int editCompanyInfo(LoanCompany company) {
        int opCount = 0;
        if (company.getId() == null) {
            opCount = companyMapper.insert(company);
        } else {
            opCount = companyMapper.updateByPrimaryKey(company);
        }
        return opCount;
    }

    @Override
    public Result checkLoginName(String loginname) {

        Result result = new Result();
        try {
            Collectors queryLevel = new Collectors();
            queryLevel.setUserSysno(loginname.toUpperCase());
            List<Collectors> levelList = collectorsMapper.select(queryLevel);

            if (CollectionUtils.isEmpty(levelList)) {
                result.setCode(Result.FAIL);
                result.setMessage("账号校验失败，账户不存在.");
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.setCode(Result.SUCCESS);
        result.setMessage("账号校验成功");
        return result;
    }

    @Override
    public Boolean isOutWorker(String userSysNo) {
        if (StringUtils.isBlank(userSysNo)) {
            return null;
        }
        Collectors c = new Collectors();
        c.setUserSysno(userSysNo);
        Collectors collectors = collectorsMapper.selectOne(c);
        return StringUtils.equals(collectors.getLevelType() + "", DclTypeEnum.Ooutter.getCode());
    }

    @Override
    public Result modifyPassword(String userNo, String oldPwd, String newPwd, String newPwdConfirm) {

        Result result = new Result();

        Collectors queryLevel = new Collectors();
        queryLevel.setUserSysno(userNo.toUpperCase());
        List<Collectors> levelList = collectorsMapper.select(queryLevel);

        if (CollectionUtils.isEmpty(levelList)) {
            result.setCode(Result.FAIL);
            result.setMessage("账户不存在.");
            return result;
        }

        Collectors Collectors = levelList.get(0);

        if (!MD5Util.encryptString(oldPwd).equals(Collectors.getPassword())) {
            result.setCode(Result.FAIL);
            result.setMessage("原始密码不正确");
            return result;
        }

        if (MD5Util.encryptString(newPwd).equals(Collectors.getPassword())) {
            result.setCode(Result.FAIL);
            result.setMessage("新密码和原始密码不能一致");
            return result;
        }

        Collectors.setPassword(MD5Util.encryptString(newPwd));

        Example example = new Example(Collectors.class);
        example.createCriteria().andEqualTo("id", Collectors.getId());

        int count = collectorsMapper.updateByExample(Collectors, example);
        if (count > 0) {
            result.setCode(Result.SUCCESS);
            result.setMessage("修改成功");
            return result;
        } else {
            result.setCode(Result.FAIL);
            result.setMessage("修改失败");
            return result;
        }
    }

    @Override
    public Result loadUserAuthInfo(String userAuth) {
        Result result = new Result();
        JSONArray authField = AuthUtil.readAuth(userAuth);
        if (authField == null) {
            result.setCode(Result.FAIL);
            result.setMessage("系统参数读取失败,请联系管理员.");
            return result;
        }

        JSONObject outJSON = new JSONObject();
        outJSON.put("auth", authField);

        result.setCode(Result.SUCCESS);
        result.setMessage("账号校验成功");
        result.setObject(outJSON);
        return result;
    }

    @Override
    public Result loadUserRoleInfo(String category) {
        Result result = new Result();
        JSONArray selfRoles = AuthUtil.readRole(category, false);
        JSONArray allRoles = AuthUtil.readRole(category, true);
        if (selfRoles == null) {
            result.setCode(Result.FAIL);
            result.setMessage("系统参数读取失败,请联系管理员.");
            return result;
        }

        JSONObject outJSON = new JSONObject();
        outJSON.put("selfRoles", selfRoles);
        outJSON.put("allRoles", allRoles);

        result.setCode(Result.SUCCESS);
        result.setMessage("账号校验成功");
        result.setObject(outJSON);
        return result;
    }

    @Override
    public Result loadLoginUser(String userName, String password, Integer source, String loginVerifyCode) {

        Result result = new Result();

        String verifyCodeKey = Constants.YM_ADMIN_SYSTEN_KEY + Constants.VERIFY_CODE;
        String userAuthKey = Constants.YM_ADMIN_SYSTEN_KEY + Constants.LOGIN_AUTH_KEY;
        userName = userName.toUpperCase();

        Collectors queryLevel = new Collectors();
        queryLevel.setUserSysno(userName);

        if (Detect.isPositive(source) && source == 1) {//有验证码登录

            String code = jedisCluster.get(verifyCodeKey + "_" + userName);
            if (StringUtils.isEmpty(code)) {
                result.setCode(Result.FAIL);
                result.setMessage("登录验证码失效,请重新获取.");
                return result;
            }

            if (!StringUtils.equals(code, loginVerifyCode)) {
                result.setCode(Result.FAIL);
                result.setMessage("登录验证码不匹配,请重新获取.");
                return result;
            }
            jedisCluster.set(verifyCodeKey + "_" + userName, "");//清空redis
        } else {

            queryLevel.setPassword(MD5Util.encryptString(password));
        }

        List<Collectors> levelList = collectorsMapper.select(queryLevel);

        if (CollectionUtils.isEmpty(levelList)) {
            result.setCode(Result.FAIL);
            result.setMessage("用户名或者密码错误");
            return result;
        }

        Collectors level = levelList.get(0);

        if (StringUtils.isEmpty(level.getStatus()) || "B".equals(level.getStatus())) {
            result.setCode(Result.FAIL);
            result.setMessage("用户状态不合法");
            return result;
        }

        //获取权限
        String authSuffix = level.getLevelType() + "." + level.getIsManage();

        JSONObject outJSON = new JSONObject();
        outJSON.put("username", level.getUserName());
        outJSON.put("userid", level.getUserSysno());
        outJSON.put("idNumber", level.getUserSysno());
        outJSON.put("auth", MD5Util.encryptStringBySign(authSuffix));
        outJSON.put("category", MD5Util.encryptStringBySign(String.valueOf(level.getLevelType())));

        result.setCode(Result.SUCCESS);
        result.setMessage("账号校验成功");
        result.setObject(outJSON);
        return result;
    }

    @Override
    public boolean isManager(String userName) {

        Collectors queryLevel = new Collectors();
        queryLevel.setUserSysno(userName);
        queryLevel.setStatus("A");

        List<Collectors> levelList = collectorsMapper.select(queryLevel);

        if (levelList != null && levelList.size() > 0) {
            Collectors level = levelList.get(0);
            return level.getIsManage() >= Constants.MANAGER_LEVEL;
        }

        return false;
    }

    @Override
    public String getUserAuth(String userName) {

        Collectors queryLevel = new Collectors();
        queryLevel.setUserSysno(userName);
        queryLevel.setStatus("A");

        List<Collectors> levelList = collectorsMapper.select(queryLevel);

        if (levelList != null && levelList.size() > 0) {
            Collectors level = levelList.get(0);
            return MD5Util.encryptStringBySign(level.getLevelType() + "." + level.getIsManage());
        }

        return null;
    }

    @Override
    public String getChannelSource(String userName) {
        return collectorsMapper.getChannelSource(userName);
    }

    /**
     * 获取渠道商扣量百分比列表
     */
    @Override
    public List<Collectors> queryChannelCollectors() {
        return collectorsMapper.queryChannelCollectors();
    }

    /**
     * 更新渠道商扣量百分比
     */
    @Override
    public ManagerResult updateChannelPercent(Collectors collectors) {
        ManagerResult managerResult  = new ManagerResult();
        try {
            collectors.setUpdateDate(new Date());
            int result =collectorsMapper.updateByPrimaryKeySelective(collectors);
            managerResult.setCode(result);
            if(result>0){
                managerResult.setMessage("处理成功！");
            }else{
                managerResult.setMessage("处理失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return managerResult;
    }

    @Override
    public Result sendVerifyCode(String userName, String password) {

        String verifyCodeKey = Constants.YM_ADMIN_SYSTEN_KEY + Constants.VERIFY_CODE;

        Result result = new Result();
        Collectors queryLevel = new Collectors();
        queryLevel.setUserSysno(userName.toUpperCase());
        Collectors Collectors = collectorsMapper.selectOne(queryLevel);

        if (Collectors == null) {
            result.setCode(Result.FAIL);
            result.setMessage("用户名错误");
            return result;
        }

        if (!MD5Util.encryptString(password).equals(Collectors.getPassword())) {
            result.setCode(Result.FAIL);
            result.setMessage("密码错误");
            return result;
        }

        if (StringUtils.isEmpty(Collectors.getPhone())) {
            result.setCode(Result.FAIL);
            result.setMessage("电话号码为空,请联系管理员");
            return result;
        }
        //生成6位验证码
        Random random = new Random();
        String radomInt = "";
        for (int i = 0; i < 6; i++) {
            radomInt += random.nextInt(10);
        }
       // String message = "【悠多多】尊敬的用户，您的登录验证码是：" + radomInt;
       // boolean success = EmaySmsUtil.send(message, Collectors.getPhone(), 1);
        ResponseDo success= smsService.sendSms(SmsTemplateEnum.CHECK_CODE_REMIND.getCode(),Collectors.getPhone(),radomInt);
        if (StateCode.SUCCESS_CODE==success.getCode()) {

            jedisCluster.set(verifyCodeKey + "_" + userName.toUpperCase(), radomInt);
            jedisCluster.expire(verifyCodeKey + "_" + userName.toUpperCase(), 60 * 10);//10min过期
            result.setCode(Result.SUCCESS);
            result.setMessage("验证码发送成功!");
            return result;
        } else {
            result.setCode(Result.FAIL);
            result.setMessage("验证码发送失败,请重新获取!");
            return result;
        }
    }

    @Override
    public List<Collectors> selectCollectors() {

        List<LoanCompany> companies = companyMapper.selectAll();
        if (companies != null && companies.size() > 0) {
            Integer companyId = null;
            for (LoanCompany company : companies) {
                if ("金互行-风控部".equals(company.getName())) {
                    companyId = company.getId();
                }
            }

            if (companyId == null) {
                return null;
            }

            Example example = new Example(Collectors.class);
            example.createCriteria()
                    .andEqualTo("userGroupId", companyId)
                    .andEqualTo("status", "A");

            return collectorsMapper.selectByExample(example);
        }
        return null;
    }
}
