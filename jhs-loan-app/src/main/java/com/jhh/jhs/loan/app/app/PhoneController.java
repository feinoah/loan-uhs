package com.jhh.jhs.loan.app.app;

import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.app.PhoneInfoService;
import com.jhh.jhs.loan.app.common.constant.Admin;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app_vo.PhoneInfoVo;
import io.github.yedaxia.apidocs.ApiDoc;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 手机信息评估
 */
@RestController
@RequestMapping("/phone")
public class PhoneController extends BaseController{

    @Autowired
    private PhoneInfoService phoneService;

    /**
     * 保存评估信息
     * @param vo
     * @param img
     * @return
     * @throws IOException
     */
    @ApiOperation(value = "保存评估信息", notes = "保存用户所填写的手机信息以及照片")
    @RequestMapping("/savePhoneInfo")
    @ApiDoc(Admin.class)
    public String savePhoneInfo(PhoneInfoVo vo,@RequestParam(required = false) MultipartFile img) throws IOException {
        NoteResult noteResult = phoneService.savePhoneInfo(vo,img == null ? null : img.getBytes());
        return JSONObject.toJSONString(noteResult);
    }
}
