package com.jhh.jhs.loan.api.app;

import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app_vo.PhoneInfoVo;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * 手机相关信息
 */
public interface PhoneInfoService {

    /**
     *  上传手机评估信息
     * @param phoneInfo
     * @param file
     * @return
     */
    public NoteResult savePhoneInfo(PhoneInfoVo vo,byte[] img);
}
