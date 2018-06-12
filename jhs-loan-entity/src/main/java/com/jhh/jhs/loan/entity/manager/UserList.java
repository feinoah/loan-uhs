package com.jhh.jhs.loan.entity.manager;

import com.jhh.jhs.loan.common.util.DateUtil;
import com.jhh.jhs.loan.entity.enums.BorrowStatusEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;


/**
 * Created by wanzezhong on 2018/1/4.
 */
@Getter @Setter @Resource
public class UserList {

    @Excel(name="手机号码")
    private String phone;
    @Excel(name="用户姓名")
    private String name;
    @Excel(name="身份证号")
    private String card_num;
    @Excel(name="黑名单")
    private String blacklist;
    @Excel(name="当前认证节点")
    private String node_code;
    @Excel(name="节点状态")
    private String node_status;
    @Excel(name="当前合同状态")
    private String borrow_status;
    @Excel(name="渠道来源")
    private String source_name;
    @Excel(name="注册时间")
    private String create_date;
    @Excel(name="认证说明")
    private String description;


    public void setBorrow_status(String borrow_status) {
        this.borrow_status = borrow_status;
        String desc = BorrowStatusEnum.getDescByCode(borrow_status);
        if(StringUtils.isNotBlank(desc)){
            this.borrow_status = desc;
        }else{
            this.borrow_status = borrow_status;
        }
    }
    public void setNode_code(String node_code) {
        if ("1".equals(node_code)){
            this.node_code = "身份证正面认证";
        }else if ("2".equals(node_code)){
            this.node_code = "身份证反面认证";
        }else if ("3".equals(node_code)){
            this.node_code = "芝麻信用";
        }else if ("4".equals(node_code)){
            this.node_code = "通讯录认证";
        }else if ("5".equals(node_code)){
            this.node_code = "个人认证";
        }else if ("6".equals(node_code)){
            this.node_code = "人脸认证";
        }else if ("7".equals(node_code)){
            this.node_code = "手机认证";
        }else if ("8".equals(node_code)){
            this.node_code = "银行卡认证";
        }
    }
    public void setBlacklist(String blacklist) {
        if ("Y".equals(blacklist)){
            this.blacklist = "是";
        }else {
            this.blacklist = "否";
        }
    }

    public void setNode_status(String node_status) {
        if ("NS001".equals(node_status)){
            this.node_status = "未认证";
        }else if ("NS002".equals(node_status)){
            this.node_status = "已认证";
        }else if ("NS003".equals(node_status)){
            this.node_status = "认证失败";
        }else if ("NS004".equals(node_status)){
            this.node_status = "已提交";
        }else {
            this.node_status = "认证失败，且进黑名单";
        }
    }


    public void setCreate_date(String create_date) {
        this.create_date = DateUtil.stampToDate(create_date);
    }
}
