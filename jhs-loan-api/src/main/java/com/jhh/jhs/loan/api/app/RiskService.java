package com.jhh.jhs.loan.api.app;

import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.Person;

import java.util.Map;

/**
 * 认证相关服务，包括芝麻、白骑士、个人节点等
 * @author xuepengfei
 */
public interface RiskService {

    /**
     * 用户认证节点的新增与修改
     * @param per_id
     * @param node_id
     * @param node_status
     * @param description
     * @return
     */
    public NoteResult createBpmNode(String per_id, int node_id, String node_status, String description);

    /**
     * 个人认证（同盾、宜信、阿福等等）
     * @param per_id
     * @param email
     * @return
     */
    public NoteResult javaCheckRisk(String per_id, String email, String tokenKey, String device);

    /**
     * 统一的hset方法   hset之前要判断key是否存在，如果key不存在会有问题。
     */
    public void jedisHset(String per_id, String key, String field, String value);

    /**
     * 初始化用户节点到缓存
     *
     * @return 用户所有节点信息
     */
    public Map<String, String> initNode(Person p);

    /**
     * 检查用户是否认证完成
     *
     * @param per_id 用户ID
     * @return
     */
    public NoteResult checkBpm(String per_id);

    /**
     * 增加调用第三方接口次数
     *
     * @param per_id
     * @param type
     * @param count
     * @param status
     * @return
     */
    public NoteResult addCount(String per_id, String type, String count, String status);

    /**
     * 获取当前节点的认证状态
     *
     * @param per_id
     * @param node_id
     * @return
     */
    public NoteResult getNodeStatus(String per_id, String node_id);

    /**
     * 芝麻信用认证
     * @param per_id
     * @return
     */
    public NoteResult zhima(String per_id);

    /**
     * 统一调风控是否黑名单方法   phone idCard 可有一个为空
     * @param phone
     * @param idCard
     * @return
     */
    public boolean isBlack(String phone, String idCard);
}
