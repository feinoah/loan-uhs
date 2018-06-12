package com.jhh.jhs.loan.mapper.app;

import com.jhh.jhs.loan.entity.app.Bpm;
import com.jhh.jhs.loan.entity.app.PersonNotify;
import org.apache.ibatis.annotations.Param;

public interface PersonNotifyMapper {

    int insert(PersonNotify record);

    int update(PersonNotify record);

    PersonNotify selectByPersonId(Integer perId);

    void resetStatusByNotifyId(String notifyId);
}