package com.jhh.jhs.loan.mapper.app;

import com.jhh.jhs.loan.entity.app.Version;
import org.apache.ibatis.annotations.Param;

/**
 * Created by OnionMac on 2018/1/26.
 */
public interface VersionMapper {

    Version getVersion(@Param("clientName") String clientName, @Param("versionName") String versionName);

    Version getMaxVersion(@Param("clientName") String clientName);
}
