package com.jhh.jhs.loan.entity.manager_vo;

import com.jhh.jhs.loan.entity.enums.BorrowStatusEnum;
import org.apache.commons.lang.StringUtils;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

//贷后管理VO
public class LoanManagementVo implements Serializable {
    @Excel(name = "逾期天数")
    private Integer bedueDays;
    @Excel(name = "姓名")
    private String customerName;
    private String customerId;
    @Excel(name = "身份证", width = 25)
    private String customerIdValue;
    @Excel(name = "手机号码", width = 15)
    private String customerMobile;
    @Excel(name="银行名称")
    private String bankName;
    @Excel(name = "银行卡号",width = 20)
    private String bankNum;
    @Excel(name = "账户余额")
    private String surplusAccountAmount;
    private String productId;
    @Excel(name = "产品类型")
    private String productName;
    @Excel(name = "产品期数")
    private String productTerm;
    @Excel(name = "借款金额")
    private String amount;
    @Excel(name = "应还租金")
    private String rental;
    @Excel(name = "应还违约金")
    private String penalty;
    @Excel(name = "手机赎回费")
    private String ransom;
    @Excel(name = "押金")
    private String deposit;
    @Excel(name = "逾期应还金额")
    private String mstRepayAmount;
    @Excel(name = "应还合计")
    private String totalAmount;
    @Excel(name = "剩余租金")
    private String surplusRental;
    @Excel(name = "剩余违约金")
    private String surplusPenalty;
    @Excel(name = "剩余手机赎回费")
    private String surplusRansom;
    @Excel(name = "剩余还款总额")
    private String surplusTotalAmount;
    @Excel(name = "到期日")
    private String endDateString;
    @Excel(name = "结清日")
    private String settleDateString;
    @Excel(name = "合同状态")
    private String stateString;

    private String auditer;
    @Excel(name = "最新催收时间")
    private String lastCallDateString;
    @Excel(name = "合同编号", width = 20)
    private String contractID;
    private String contractKey;
    @Excel(name = "放款金额")
    private String loanAmount;
    @Excel(name = "已还款")
    private String repayAmount;

    private String payAmount;
    @Excel(name = "已减免")
    private String reduceAmount;
    private String createUser;
    @Excel(name = "最新扣款时间")
    private String orderString;
    @Excel(name = "是否黑名单")
    private String blackList;

    private Integer isManual;

    private String description;

    private String borrStatus;
    private String status;
    private String reason;
    @Excel(name = "催收人")
    private String userName;
    private String serviceAmount;
    public String getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(String payAmount) {
        this.payAmount = payAmount;
    }

    public String getReduceAmount() {
        return reduceAmount;
    }

    public void setReduceAmount(String reduceAmount) {
        this.reduceAmount = reduceAmount;
    }

    public String getBorrStatus() {
        return borrStatus;
    }

    public void setBorrStatus(String borrStatus) {
        this.borrStatus = borrStatus;
        this.setStateString(borrStatus);
    }

    public void setStateString(String stateString) {
        String desc = BorrowStatusEnum.getDescByCode(stateString);
        if (StringUtils.isNotBlank(desc)) {
            this.stateString = desc;
        } else {
            this.stateString = stateString;
        }
    }

    public Integer getBedueDays() {
        return bedueDays;
    }

    public void setBedueDays(Integer bedueDays) {
        this.bedueDays = bedueDays;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerIdValue() {
        return customerIdValue;
    }

    public void setCustomerIdValue(String customerIdValue) {
        this.customerIdValue = customerIdValue;
    }

    public String getCustomerMobile() {
        return customerMobile;
    }

    public void setCustomerMobile(String customerMobile) {
        this.customerMobile = customerMobile;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankNum() {
        return bankNum;
    }

    public void setBankNum(String bankNum) {
        this.bankNum = bankNum;
    }

    public String getSurplusAccountAmount() {
        return surplusAccountAmount;
    }

    public void setSurplusAccountAmount(String surplusAccountAmount) {
        this.surplusAccountAmount = surplusAccountAmount;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductTerm() {
        return productTerm;
    }

    public void setProductTerm(String productTerm) {
        this.productTerm = productTerm;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getRental() {
        return rental;
    }

    public void setRental(String rental) {
        this.rental = rental;
    }

    public String getPenalty() {
        return penalty;
    }

    public void setPenalty(String penalty) {
        this.penalty = penalty;
    }

    public String getRansom() {
        return ransom;
    }

    public void setRansom(String ransom) {
        this.ransom = ransom;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getSurplusRental() {
        return surplusRental;
    }

    public void setSurplusRental(String surplusRental) {
        this.surplusRental = surplusRental;
    }

    public String getSurplusPenalty() {
        return surplusPenalty;
    }

    public void setSurplusPenalty(String surplusPenalty) {
        this.surplusPenalty = surplusPenalty;
    }

    public String getSurplusRansom() {
        return surplusRansom;
    }

    public void setSurplusRansom(String surplusRansom) {
        this.surplusRansom = surplusRansom;
    }

    public String getSurplusTotalAmount() {
        return surplusTotalAmount;
    }

    public void setSurplusTotalAmount(String surplusTotalAmount) {
        this.surplusTotalAmount = surplusTotalAmount;
    }

    public String getEndDateString() {
        return endDateString;
    }

    public void setEndDateString(String endDateString) {
        this.endDateString = endDateString;
    }

    public String getSettleDateString() {
        return settleDateString;
    }

    public void setSettleDateString(String settleDateString) {
        this.settleDateString = settleDateString;
    }

    public String getStateString() {
        return stateString;
    }

    public String getAuditer() {
        return auditer;
    }

    public void setAuditer(String auditer) {
        this.auditer = auditer;
    }

    public String getLastCallDateString() {
        return lastCallDateString;
    }

    public void setLastCallDateString(String lastCallDateString) {
        this.lastCallDateString = lastCallDateString;
    }

    public String getContractID() {
        return contractID;
    }

    public void setContractID(String contractID) {
        this.contractID = contractID;
    }

    public String getContractKey() {
        return contractKey;
    }

    public void setContractKey(String contractKey) {
        this.contractKey = contractKey;
    }

    public String getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(String loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getRepayAmount() {
        return repayAmount;
    }

    public void setRepayAmount(String repayAmount) {
        this.repayAmount = repayAmount;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getOrderString() {
        return orderString;
    }

    public void setOrderString(String orderString) {
        this.orderString = orderString;
    }

    public String getBlackList() {
        return blackList;
    }

    public void setBlackList(String blackList) {
        this.blackList = blackList;
    }

    public Integer getIsManual() {
        return isManual;
    }

    public void setIsManual(Integer isManual) {
        this.isManual = isManual;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMstRepayAmount() {
        return mstRepayAmount;
    }

    public void setMstRepayAmount(String mstRepayAmount) {
        this.mstRepayAmount = mstRepayAmount;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getServiceAmount() {
        return serviceAmount;
    }

    public void setServiceAmount(String serviceAmount) {
        this.serviceAmount = serviceAmount;
    }
}
