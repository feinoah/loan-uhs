package com.jhh.jhs.loan.service.haier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.common.util.DateUtil;
import com.jhh.jhs.loan.constant.HaierConstants;
import com.jhh.jhs.loan.entity.HaierBaseVo;
import com.jhh.jhs.loan.entity.HaierRefundVo;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;

@Service
public class HaierRefundServiceImpl extends HaierBaseServiceImpl implements HaierRefundService {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(HaierRefundServiceImpl.class);

    @Override
    public NoteResult partnerBankRefund(HaierRefundVo refund) throws Exception {
        logger.info("海尔代付进入成功 ，基本请求参数为" + refund);
        NoteResult noteResult = new NoteResult(Constants.CODE_201, "处理异常");
        if (!ObjectUtils.isEmpty(refund)) {
            JSONObject bizReq = (JSONObject) JSON.toJSON(refund);
            logger.info("基本请求参数转换json为 bizReq = " + bizReq);
            if (!ObjectUtils.isEmpty(bizReq)) {
                HaierBaseVo base = new HaierBaseVo();
                base.setService(HaierConstants.SERVICE_PAYMENT);
                base.setRequest_no(refund.getOut_trade_no());
                base.setTimestamp(DateUtil.getDateStringToHHmmss(new Date()));
                String biz_content = encrypt(base.getSign_type(), bizReq.toString(), base.getCharset());

                logger.info("代付基本参数加密串为 biz_content = {} ", biz_content);

                base.setBiz_content(biz_content);
                //调用第三方接口
                String response = post(base);
                //验证返回参数
                validResponse(response, noteResult);
            }
        }
        return noteResult;
    }
}
