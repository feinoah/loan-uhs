package com.jhh.jhs.loan.service.haier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.common.util.DateUtil;
import com.jhh.jhs.loan.constant.HaierConstants;
import com.jhh.jhs.loan.entity.HaierBaseVo;
import com.jhh.jhs.loan.entity.HaierDeductVo;
import com.jhh.jhs.loan.entity.HaierPayVo;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 代付相关接口
 */
@Service
public class HaierPayServiceImpl extends HaierBaseServiceImpl implements HaierPayService {

    private static final Logger logger = LoggerFactory.getLogger(HaierPayServiceImpl.class);

    /***
     *   银行卡代付 网关接口
     * @param vo 代付请求参数
     * @return 响应
     */
    @Override
    public NoteResult partnerBankPaying(HaierPayVo vo) throws Exception {
        logger.info("海尔代付进入成功 ，基本请求参数为" + vo);
        NoteResult noteResult = new NoteResult(Constants.CODE_201, "处理异常");
        if (vo != null) {
            JSONObject bizReq = (JSONObject) JSON.toJSON(vo);
            logger.info("基本请求参数转换json为 bizReq = " + bizReq);
            if (bizReq != null) {
                HaierBaseVo base = new HaierBaseVo();
                base.setService(HaierConstants.SERVICE_PAYMENT);
                base.setRequest_no(vo.getOut_trade_no());
                base.setTimestamp(DateUtil.getDateStringToHHmmss(new Date()));
                String biz_content = encrypt(base.getSign_type(), bizReq.toString(), base.getCharset());
                logger.info("代付基本参数加密串为 biz_content" + biz_content);
                base.setBiz_content(biz_content);
                //调用第三方接口
                String response = post(base);
                //验证返回参数
                validResponse(response,noteResult);
            }
        }
        return noteResult;
    }


}
