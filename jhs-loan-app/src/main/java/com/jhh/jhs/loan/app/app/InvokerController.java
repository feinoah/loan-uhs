package com.jhh.jhs.loan.app.app;

import com.jhh.jhs.loan.api.common.InvokerComponentService;
import com.jhh.jhs.loan.entity.app.NoteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class InvokerController {

    @Autowired
    private InvokerComponentService invokerComponentService;

    @RequestMapping("/health")
    public NoteResult invoke() {
        return invokerComponentService.invokeComponent();
    }

}
