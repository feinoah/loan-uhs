package com.jhh.jhs.loan.manage.service.common;

import com.jhh.jhs.loan.entity.app.NoteResult;

public interface InvokerService {

    /**
     * 调用各个组件
     * @return
     */
    NoteResult invokeComponent();
}
