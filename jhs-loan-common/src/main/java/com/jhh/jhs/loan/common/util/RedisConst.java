package com.jhh.jhs.loan.common.util;

/**
 * redis 中的key常量
 * Created by xuepengfei on 2017/7/20.
 */
public class RedisConst {
    public static final String SEPARATOR = ":";

    public static final String BP_KEY = "JHS_a_bp_";
    public static final String PR_KEY = "JHS_a_pr_t_";
    public static final String CITY_KEY = "JHS_a_app_get_city";
    public static final String REVIEW_KEY = "JHS_a_sng_rew_";
    public static final String ORDER_KEY = "JHS_a_SCZ_oid_";
    public static final String ORDER_REFUND_KEY = "JHS_a_SCZ_refund_";
    public static final String REPAYMENT_KEY = "JHS_a_SCZ_rnt_";
    public static final String PAYCONT_KEY = "JHS_a_PC_bid_";
    public static final String REGISTER_KEY = "JHS_a_UR_";
    public static final String DOWNLOAD_KEY = "JHS_a_app_dad_aod";
    public static final String PRODUCT_KEY = "JHS_a_app_pct_info";
    public static final String BANKLIST_KEY = "JHS_a_srt_bank_list";
    public static final String QUESTION_KEY = "JHS_a_con_qns";
    public static final String NODE_KEY = "JHS_a_nds_";
    public static final String CONTACTS_NUM_KEY = "JHS_a_cont_num_";
    public static final String PAY_ORDER_KEY = "JHS_a_bc_tag_";
    public static final String PAY_REFUND_KEY = "JHS_a_refund_tag_";
    public static final String JUXINLI_TOKEN = "JHS_a_jxl_tk_";
    public static final String CONTACT_LIMIT = "JHS_a_contact_lmt";
    public static final String NOT_EXIST_ORDERS = "JHS_a_not_exist_orders";
    public static final String AMOUNT_LIMIT = "JHS_a_amount_limit";
    public static final String DELAY_QUEUE = "JHS_a_delay_queue";
    public static final String QUEUE_ELEMENT_EXIST = "JHS_a_q_e_exist_";
    public static final String ORDER_QUEUE_NAME = "JHS_a_order_queue";
    public static final String SHARE_RULES = "JHS_a_share_rules";
    public static final String SHARE_PATH = "JHS_a_share_url";
    public static final String SHARE_PATH_RULES = "JHS_a_share_url_rules"; // 分享规则流程图片
    public static final String SHARE_DATA = "JHS_a_share_data";
    public static final String COMMISSION_MINIMUM_AMOUNT = "JHS_a_commission_min";

    public static final String AGENT_PAY_QUERY_YSB = "AGENT_PAY_QUERY_YSB_";
    public static final String AGENT_PAY_QUERY_JHH_YSB = "AGENT_PAY_QUERY_JHH_YSB";
    // 清结算锁
    public static final String SETTLE_LOCK_KEY = "zl:st:zl_bt:st:power:switch";

    public static final String DRAINAGE_STAT = "JHS_DRAINAGE_STAT";


    //当前认证流程
    public static final String BPM_LIST_KEY = "JHS_a_bpm_list";

    public static final String PAY_TYPE_KEY = "JHS_a_payType";

    public static final String DEDUCT_TYPE_KEY = "JHS_a_deductType";
    // 退款redis支付渠道key
    public static final String REFUND_TYPE_KEY = "JHS_a_refundType";

    //还款流水时间限制key
    public static final String ORDER_HISTORY_QUERY_TIME = "JHS_order_his_query_time";
    //还款流水单
    public static final String ORDER_HISTORY_KEY = "JHS_order_his";

    //支付渠道_拉卡拉
    public static final String PAY_CHANNEL_LKL = "JHS_pay_channel_lkl";

    //提现操作锁
    public static final String COMMISSION_WITHDRAWAL_LOCK = "JSH_COMMISSION_WITHDRAW_";

    // 用户验证码
    public static final String VALIDATE_CODE = "JHS_VALIDATE_CODE";

    // 合利宝获取短信全局自增订单号
    public static final String HELI_PAY_MSG_ORDER_NUM = "JHS_HELI_PAY_MSG_ORDER_NUM";

    public static final String BATCHDEDUCT_LOCK= "JHS_batchDeduct_click_sleep";

    //注册提醒通知
    public static final String REGISTER_REMIND_NOTICE  = "JHS_register_notice";
    //登录提醒通知
    public static final String LOGIN_REMIND_NOTICE = "JHS_login_notice";
    //注册提醒通知锁
    public static final String REGISTER_REMIND_NOTICE_LOCK  = "JHS_register_notice_lock";
    //登录提醒通知锁
    public static final String LOGIN_REMIND_NOTICE_LOCK = "JHS_login_notice_lock";


    //百可录是否打电话总开关
    public static final String BAIKELU_ALL_IS_OPEN_KEY="JHS_baikelu_all_onOff";

}
