package com.jhh.jhs.loan.api.app;

import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.manager.CodeValue;
import com.jhh.jhs.loan.entity.share.ShareData;

import java.util.List;

public interface ShareService {

    /**
     * 获取活动规则
     * @return
     */
    List<CodeValue> getRules();

    /**
     * 获取分享活动banner流程图 image 地址
     */
    String getShareProcessUrl();

    /**
     * 好友邀请列表
     * @param perId person id
     * @param nowPage 当前页
     * @param pageSize 页数
     */
    NoteResult inviterLevel(String perId, String level,String nowPage, String pageSize);

    /**
     * 分享出去的数据
     */
    ShareData shareUrlData(String perId);
}
