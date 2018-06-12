package com.jhh.jhs.loan.service.haier;

import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.common.util.PropertiesReaderUtil;
import com.jhh.jhs.loan.constant.HaierConstants;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.service.capital.BasePayServiceImpl;
import com.jhh.jhs.loan.service.capital.BaseServiceImpl;
import com.jhh.jhs.loan.util.HttpClientUtilsExt;
import com.jhh.jhs.loan.util.MapUtils;
import com.kjtpay.gateway.common.util.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 通用方法类
 */
@Service
public class HaierBaseServiceImpl extends BasePayServiceImpl{

    private String gatewayUrl = PropertiesReaderUtil.read("haier", "gatewayUrl");

    private static final Logger logger = LoggerFactory.getLogger(HaierBaseServiceImpl.class);

    @Autowired
    SecurityService securityService;
    /**
     * 向第三方发送请求
     */
    public String post(Object obj) throws Exception {
        //将参数放入map
        Map<String, String> sParaTemp = MapUtils.setConditionMap(obj);
        sParaTemp.put("sign",securityService.sign(sParaTemp, sParaTemp.get("charset")));
        //发送post请求
        String response = HttpClientUtilsExt.post(gatewayUrl, sParaTemp,sParaTemp.get("charset"));
        logger.info("--------------------->>>>>>>>调用海尔三方响应参数--------------<<<<<<<<<\nresponse=" + response);
        return response;
    }

    public void validResponse(String response,NoteResult noteResult){
        //返回格式转换
        JSONObject result = JSONObject.parseObject(response);
        //判断调用是否成功
        String code = result.getString("code");
        if (HaierConstants.SUCCESS_CODE.equals(code)) {
            String status = result.getJSONObject("biz_content").getString("status");
            if (HaierConstants.SUCCESS_STATUS.equals(status)){
                noteResult.setCode(HaierConstants.SUCCESS);
                noteResult.setInfo(HaierConstants.SUCCESS_VALUE);
            }else if (HaierConstants.DEAL_STATUS.equals(status)){
                noteResult.setCode(HaierConstants.WAIT);
                noteResult.setInfo("处理中");
            }else {
                noteResult.setInfo(result.getString("sub_msg"));
            }
        } else {
            noteResult.setInfo(result.getString("sub_msg"));
        }
    }

    /**
     * 使用json字符串形式加密
     * @param signType
     * @param oriText
     * @param charset
     * @return
     */
    public String encrypt(String signType, String oriText, String charset){
        //演示使用字符串形式加密
        if("RSA".equals(signType)){
            //RSA加密
            return securityService.encrypt(oriText, charset);
        }else if("ITRUS".equals(signType)){
            return securityService.encrypt(oriText, charset);
        }else{
            return "加密出错";
        }

    }
}
