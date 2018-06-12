package com.jhh.jhs.loan.entity.app;

import com.jhh.jhs.loan.common.util.BorrNum_util;
import com.jhh.jhs.loan.common.util.CodeReturn;
import com.jhh.jhs.loan.common.util.DateUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "borrow_list")
@Getter
@Setter
@ToString
public class BorrowList implements Serializable {

    public static final int SEND_LOAN_MESSAGE = 1 << 2;

    private static final long serialVersionUID = 2510888469306589931L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer perId;

    private Integer prodId;

    private String borrType;

    private Date askborrDate;

    private String borrNum;

    private Date makeborrDate;

    private Date payDate;

    private Date planrepayDate;

    private Date actRepayDate;

    private String borrStatus;

    private Integer overdueDays;

    private Float penalty;

    private Float surplusPenalty;

    private Float planRental;

    private Float planRepay;

    private Float actPlanAmount;

    private Float actRepayAmount;

    private Float actReduceAmount;

    private Float surplusAmount;

    private Integer ispay;

    private Integer termNum;

    private Integer totalTermNum;
    
    private Float borrAmount;

    private Integer noDepositRefund;

    private Float depositAmount;

    private Float payAmount;

    private Float ransomAmount;

    private Float surplusRansomAmount;

    private Float surplusRentalAmount;

    private Float penaltyRate;

    private Integer perCouponId;

    private Date currentRepayTime;

    private String collectionUser;

    private Date currentCollectionTime;

    private Date updateDate;

    private Integer updateUser;

    private Date creationDate;

    private Integer creationUser;

    private Integer version;

    private Integer baikeluStatus;

    private String borrUpStatus;

    private String description;

    private Integer contactNum;

    private Integer isManual;

    private Integer flag;

    private Float serviceAmount;

    public BorrowList(){

    }

    /**
     * 申请借款订单生产
     */
    public BorrowList(Product product,String perId,String borrUpStatus){
        this.perId = Integer.parseInt(perId);
        this.prodId = product.getId();
        this.askborrDate = new Date();
        this.borrNum = "JHS" + BorrNum_util.getStringRandom(12);
        this.borrStatus = CodeReturn.STATUS_APLLY;
        this.termNum = product.getTerm();
        this.totalTermNum = product.getTerm();
        this.borrAmount = product.getAmount();
        this.depositAmount = product.getDeposit();
        this.ransomAmount = product.getRansom();
        this.penaltyRate = product.getPenaltyRate();
        this.planRental = product.getRent()*product.getTerm();
        this.payAmount = product.getAmount()-product.getDeposit()-product.getServiceAmount();
        if (StringUtils.isNotBlank(borrUpStatus)){
            this.borrUpStatus = borrUpStatus;
        }
        this.serviceAmount = product.getServiceAmount();
    }
}