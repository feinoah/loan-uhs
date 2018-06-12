package com.jhh.jhs.loan.api.app;

import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.PersonNotify;

public interface NotifyService {

	/**
	 * 注册用户推送
	 * @param personNotify 用户推送注册信息
	 * @return 返回用户推送注册请求状态
	 */
	public NoteResult registerPersonNotify(PersonNotify personNotify);

	/**
	 * 解绑用户推送
	 * @param personNotify 用户推送注册信息
	 * @return 返回用户推送解绑请求状态
	 */
	public NoteResult unregisterPersonNotify(PersonNotify personNotify);
}