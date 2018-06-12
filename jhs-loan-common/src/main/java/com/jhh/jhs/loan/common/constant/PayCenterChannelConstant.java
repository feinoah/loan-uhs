package com.jhh.jhs.loan.common.constant;

/**
 * 支付渠道常量类
 *
 * @author wenfucheng
 */
public interface PayCenterChannelConstant {

    /**
     * 银生宝
     */
    String PAY_CHANNEL_YSB = "ysb-rong";

    /**
     * 合利宝(普通支付)
     */
    String PAY_CHANNEL_HLB = "pay-helipay";

    /**
     * 合利宝(快捷支付)
     */
    String PAY_CHANNEL_HLB_QUICK = "pay-helipay-quick";

    /**
     * 拉卡拉
     */
    String PAY_CHANNEL_LKL = "pay-lkl";

    /**
     * 支付宝
     */
    String PAY_CHANNEL_ZFB = "pay-zfb";

    /**
     * 业务默认渠道
     */
    String PAY_CHANNEL_DEFAULT = "";
}
