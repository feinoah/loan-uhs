package com.jhh.jhs.loan.service.app;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.jhh.jhs.loan.api.app.NotifyService;
import com.jhh.jhs.loan.common.util.CodeReturn;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.PersonNotify;
import com.jhh.jhs.loan.mapper.app.PersonNotifyMapper;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 用户通知推送接口实现类
 *
 * @author chenchao
 */

@Service
public class NotifyServiceImpl implements NotifyService {

    private static final Logger logger = LoggerFactory.getLogger(NotifyServiceImpl.class);

    private static final String SUCCESS_CODE = CodeReturn.SUCCESS_CODE;
    private static final String SUCCESS_INFO = "成功";
    private static final String FAIL_CODE = CodeReturn.FAIL_CODE;
    private static final String FAIL_INFO = "失败";

    @Autowired
    private PersonNotifyMapper personNotifyMapper;

    @Override
    public NoteResult registerPersonNotify(PersonNotify personNotify) {

        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);

        try {
            if (personNotify != null && personNotify.getPerId() != null && personNotify.getNotifyId() != null) {
                PersonNotify queryResult = personNotifyMapper
                        .selectByPersonId(personNotify.getPerId());
                if (queryResult == null) {
                    personNotifyMapper.resetStatusByNotifyId(personNotify.getNotifyId());
                    int resultCode = personNotifyMapper.insert(personNotify);
                    if (resultCode > 0) {
                        result.setCode(SUCCESS_CODE);
                        result.setInfo(SUCCESS_INFO);
                    }
                } else {
                    //状态不同时更新数据库
                    if (personNotify.getNotifyId().equals(queryResult.getNotifyId()) &&
                            personNotify.getStatus().equals(queryResult.getStatus())) {
                        result.setCode(SUCCESS_CODE);
                        result.setInfo(SUCCESS_INFO);
                    } else {
                        personNotifyMapper.resetStatusByNotifyId(personNotify.getNotifyId());
                        int resultCode = personNotifyMapper.update(personNotify);
                        if (resultCode > 0) {
                            result.setCode(SUCCESS_CODE);
                            result.setInfo(SUCCESS_INFO);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            logger.error("持久化PersonNotify失败", e);
        }

        return result;
    }

    @Override
    public NoteResult unregisterPersonNotify(PersonNotify personNotify) {
        NoteResult result = new NoteResult(FAIL_CODE, FAIL_INFO);

        if (personNotify != null && personNotify.getPerId() != null) {
            try {
                int resultCode = personNotifyMapper.update(personNotify);
                if (resultCode > 0) {
                    result.setCode(SUCCESS_CODE);
                    result.setInfo(SUCCESS_INFO);
                }
            } catch (Throwable e) {
                logger.error("持久化PersonNotify失败", e);
            }
        }

        return result;
    }
}
