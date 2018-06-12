package com.jhh.jhs.loan.entity;

import com.jhh.jhs.loan.entity.app.BorrowList;
import com.jhh.jhs.loan.entity.app.Person;
import com.jhh.jhs.loan.entity.utils.RepaymentDetails;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 2018/4/9.
 */
@Data
@AllArgsConstructor
public class PersonInfoDto {

    private BorrowList borrowList;

    private Person person;

    private RepaymentDetails repaymentDetails;
}
