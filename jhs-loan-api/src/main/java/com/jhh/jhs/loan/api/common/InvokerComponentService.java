package com.jhh.jhs.loan.api.common;

import com.jhh.jhs.loan.entity.app.NoteResult;

/**
 * 调度链服务
 */
public interface InvokerComponentService {

    /**
     * 调用各个组件
     * @return
     */
    NoteResult invokeComponent();
}
