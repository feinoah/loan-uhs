package com.jhh.jhs.loan.service.phone;

import com.alibaba.dubbo.config.annotation.Service;
import com.jhh.jhs.loan.api.app.PhoneInfoService;
import com.jhh.jhs.loan.common.util.DFSUtil;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.PhoneInfo;
import com.jhh.jhs.loan.entity.app_vo.PhoneInfoVo;
import com.jhh.jhs.loan.mapper.phone.PhoneInfoMapper;
import com.jhh.jhs.loan.service.app.VerifyServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.util.Date;

/**
 * 2018/1/16.
 */
@Service
@Slf4j
public class PhoneInfoServiceImpl implements PhoneInfoService {

    @Autowired
    private VerifyServiceImpl verifyService;

    @Autowired
    private PhoneInfoMapper phoneInfoMapper;

    @Value("${dfsUrl}")
    private String dfsUrl;

    @Override
    public NoteResult savePhoneInfo(PhoneInfoVo vo,byte[] img) {
        log.info("保存用户手机评估信息参数 PhoneInfoVo "+vo);
        NoteResult noteResult;
        String path = null;
        //验证用户是否存在
        if (!verifyService.verifyPerson(vo.getPer_id())){
            return NoteResult.FAIL_RESPONSE("用户不存在");
        }
        if ("ios".equals(vo.getDevice())){
            if (img == null){
                return NoteResult.FAIL_RESPONSE("请上传手机序列号图片");
            }else {
                path = DFSUtil.uploadByteArray(dfsUrl,img,"jpg");
                //上传评估图片
                if (StringUtils.isEmpty(path)){
                    return NoteResult.FAIL_RESPONSE("图片上传失败");
                }
            }
        }else {
            if (StringUtils.isEmpty(vo.getSequence())){
                return NoteResult.FAIL_RESPONSE("请上传手机序列号");
            }
        }
        //保存评估信息
        PhoneInfo info = new PhoneInfo(vo,path);
        int i = saveOrUpdate(info);
        if (i == 0){
            noteResult = NoteResult.FAIL_RESPONSE("保存失败");
        }else {
            noteResult = NoteResult.SUCCESS_RESPONSE();
        }
        return noteResult;
    }


    /**
     *  更新或保存手机评估信息
     * @param info
     * @return
     */
    public int saveOrUpdate(PhoneInfo info){
        int i;
        if (info == null){
            i = 0;
        }else {
            if (info.getId() == null){
              i = phoneInfoMapper.insertSelective(info);
            }else {
              i = phoneInfoMapper.updateByPrimaryKeySelective(info);
            }
        }
        return i;
    }
}
