package com.jhh.jhs.loan.service.app;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.jhh.jhs.loan.api.app.ShareService;
import com.jhh.jhs.loan.common.util.RedisConst;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.manager.CodeValue;
import com.jhh.jhs.loan.entity.manager_vo.CommissionDetailVo;
import com.jhh.jhs.loan.entity.share.ShareData;
import com.jhh.jhs.loan.mapper.app.CodeValueMapper;
import com.jhh.jhs.loan.mapper.share.CommissionOrderMapper;
import com.jhh.jhs.loan.util.CodeValueUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.JedisCluster;

import java.util.List;

/**
 * @author xingmin
 */
@Service
public class ShareServiceImpl implements ShareService {

    private static final Logger logger = LoggerFactory.getLogger(ShareServiceImpl.class);

    @Autowired
    private CodeValueMapper codeValueMapper;
    @Autowired
    private JedisCluster jedisCluster;
    @Autowired
    private CommissionOrderMapper commissionOrderMapper;

    @Override
    public List<CodeValue> getRules() {
        String rules = jedisCluster.get(RedisConst.SHARE_RULES);
        if (!StringUtils.isEmpty(rules)) {
            JSONArray array = JSONArray.parseArray(rules);
            return array.toJavaList(CodeValue.class);
        }

        List<CodeValue> list = codeValueMapper.getEnabledCodeValues("share_rules");
        if (list.size() < 1) {
            return null;
        }

        jedisCluster.setex(RedisConst.SHARE_RULES, 30 * 24 * 60 * 60, JSONArray.toJSONString(list));
        return list;
    }

    @Override
    public String getShareProcessUrl() {

        String imageUrl = jedisCluster.get(RedisConst.SHARE_PATH_RULES);
        if (StringUtils.isNotBlank(imageUrl)) {
            return imageUrl;
        }
        List<CodeValue> list = codeValueMapper.getEnabledCodeValues("share_rules_process_url");
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        imageUrl = list.get(0).getCodeCode();
        jedisCluster.setex(RedisConst.SHARE_PATH_RULES, 30 * 24 * 60 * 60, imageUrl);

        return imageUrl;
    }

    @Override
    public NoteResult inviterLevel(String perId, String level, String nowPage, String pageSize) {
        try {
            int start = (Integer.parseInt(nowPage) - 1) * Integer.parseInt(pageSize);

            List<CommissionDetailVo> vos = commissionOrderMapper.queryLevelCommissionOrderByPersonId(perId, level, start, Integer.parseInt(pageSize));

            NoteResult result = NoteResult.SUCCESS_RESPONSE();
            result.setData(CollectionUtils.isEmpty(vos) ? vos : convert(vos));
            return result;

        } catch (Exception e) {
            logger.error("调用失败，异常信息 {}", e);
            e.printStackTrace();
            return NoteResult.FAIL_RESPONSE("系统繁忙,请稍候再试");
        }
    }

    private List<CommissionDetailVo> convert(List<CommissionDetailVo> vos) {
        vos.forEach(vo -> {
            String phone = vo.getPhone();
            vo.setPhone(phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2").trim());
            if ("android".equals(vo.getType())) {
                vo.setType("安卓");
            } else if ("ios".equals(vo.getType())) {
                vo.setType("苹果");
            }
        });
        return vos;
    }

    @Override
    public ShareData shareUrlData(String perId) {
        logger.info("开始查询分享数据...");

        String data = jedisCluster.get(RedisConst.SHARE_DATA);
        if (!StringUtils.isEmpty(data)) {
            ShareData shareData = JSON.parseObject(data, ShareData.class);
            shareData.setUrl(getShareUrl(perId));

            return shareData;
        }

        List<CodeValue> title = codeValueMapper.getEnabledCodeValues("share_data_title");
        List<CodeValue> secTitle = codeValueMapper.getEnabledCodeValues("share_data_secTitle");
        List<CodeValue> dataLogo = codeValueMapper.getEnabledCodeValues("share_data_logo");
        List<CodeValue> description = codeValueMapper.getEnabledCodeValues("share_data_description");

        if (CollectionUtils.isEmpty(title) ||
                CollectionUtils.isEmpty(secTitle) ||
                CollectionUtils.isEmpty(dataLogo) ||
                CollectionUtils.isEmpty(description)) {
            return null;
        }

        ShareData shareData = new ShareData();
        shareData.setDescriptions(description.get(0).getCodeCode());
        shareData.setLogo(dataLogo.get(0).getCodeCode());
        shareData.setSecTitle(secTitle.get(0).getCodeCode());
        shareData.setTitle(title.get(0).getCodeCode());
        shareData.setUrl(getShareUrl(perId));
        jedisCluster.setex(RedisConst.SHARE_DATA,30 * 24 * 60 * 60,JSON.toJSONString(shareData));

        logger.info("结束查询分享的数据 = {}",JSON.toJSONString(shareData));
        return shareData;
    }

    /**
     * 获取分享按钮url
     *
     * @param source
     * @return
     */
    private String getShareUrl(String source) {
        String value = CodeValueUtil.getCodeValueFromRedis(RedisConst.SHARE_PATH, 30 * 24 * 60 * 60, "share_url");
        if (StringUtils.isEmpty(value)) {
            return null;

        }
        return String.format("%s?source=%s", value, source);
    }
}
