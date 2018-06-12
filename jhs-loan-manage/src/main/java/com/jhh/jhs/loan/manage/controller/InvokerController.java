package com.jhh.jhs.loan.manage.controller;

import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.manage.service.common.InvokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调用链实现
 */
@RestController
@RequestMapping("/manage")
public class InvokerController {

    @Autowired
    private InvokerService invokerService;

    @RequestMapping("/health")
    public NoteResult invoke() {
        return invokerService.invokeComponent();
    }


}
