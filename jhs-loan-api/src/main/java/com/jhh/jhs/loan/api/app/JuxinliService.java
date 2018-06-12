package com.jhh.jhs.loan.api.app;

import com.jhh.jhs.loan.entity.Juxinli.ReqDtoBasicInfo;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.manager_vo.ReqBackPhoneCheckVo;

/**聚信立相关service
 * @author
 */
public interface JuxinliService {
    /**聚信立结果回调
     * @param record
     * @return
     */
    public NoteResult backPhoneCheckMessage(ReqBackPhoneCheckVo record);

    /**APP端发起聚信立认证请求
     * 通用接口 所有APP端有关手机认证的统一接口
     * 包括 提交服务密码 提交短信验证码 重新提交短信验证码
     * @param reqDtoBasicInfo
     * @return
     */
    public NoteResult risk(ReqDtoBasicInfo reqDtoBasicInfo);

}
