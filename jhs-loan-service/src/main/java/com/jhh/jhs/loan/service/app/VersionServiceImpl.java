package com.jhh.jhs.loan.service.app;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.jhh.jhs.loan.api.app.VersionService;
import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.entity.app.Version;
import com.jhh.jhs.loan.mapper.app.VersionMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by OnionMac on 2018/1/26.
 */
@Service
public class VersionServiceImpl implements VersionService {

    private static final Logger logger = LoggerFactory.getLogger(VersionServiceImpl.class);

    @Autowired
    VersionMapper mVersionMapper;

    @Override
    public ResponseDo<Version> getVersionByVersionName(String clientName, String versionName) {

        ResponseDo<Version> responseDo = new ResponseDo<>();
        Version version;
        try {
            version = mVersionMapper.getVersion(clientName, versionName);

            if(version != null){
                responseDo.setCode(200);
                responseDo.setInfo("成功");
                responseDo.setData(version);
            }else{
                if(Version.VERSIONNAME_ANDROID.equals(clientName)){
                    logger.info("查询android最大的版本");
                    version = mVersionMapper.getMaxVersion(clientName);
                    responseDo.setData(version);
                }
                responseDo.setCode(203);
                responseDo.setInfo("强制更新");
            }

        }catch (Exception e){
            e.printStackTrace();
            responseDo.setCode(201);
            responseDo.setInfo("系统繁忙");
        }

        return responseDo;
    }
}
