package com.jhh.jhs.loan.api.app;

import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.entity.app.Version;


/**版本更新接口
 * @author xuepengfei
 */
public interface VersionService {

    /**根据版本号及设备 查询最新版本
     * @param clientName
     * @param versionName
     * @return
     */
    ResponseDo<Version> getVersionByVersionName(String clientName, String versionName);

}
