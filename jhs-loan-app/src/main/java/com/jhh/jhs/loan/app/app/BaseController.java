package com.jhh.jhs.loan.app.app;

import com.jhh.jhs.loan.app.common.exception.CommonException;
import com.jhh.jhs.loan.entity.app.NoteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  异常捕获公用类
 */
@Controller
@Slf4j
public class BaseController {

    @ExceptionHandler
    @ResponseBody
    public NoteResult exception(HttpServletRequest request, Exception ex) {
        if (ex instanceof CommonException) {
            log.info("自定义异常抛出   CommonException=",ex);
            CommonException commonException = (CommonException) ex;
            return new NoteResult(commonException.getResultCode(),
                    commonException.getMessage());
        }
        // 打印
        log.info(ex.getMessage(), ex);
        return NoteResult.FAIL_RESPONSE("系统繁忙");
    }

    protected Map<String, String> getResult(List<BindingResult> bindingResults) {
        Map<String, String> map = new HashMap<>();
        for (BindingResult bindingResult : bindingResults) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                map.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        }
        return map;
    }
}
