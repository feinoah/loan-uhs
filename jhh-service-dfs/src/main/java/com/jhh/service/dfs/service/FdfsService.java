package com.jhh.service.dfs.service;

import com.jhh.jhs.loan.entity.app.NoteResult;

import java.io.InputStream;

/**
 * FDFS文件服务接口
 *
 * @author chenchao
 * @date 2018/1/2
 */
public interface FdfsService {

    /**
     * 上传文件到DFS
     * @param fileStream 文件流
     * @param fileExtName 文件扩展名
     * @return
     */
    public NoteResult uploadFile(InputStream fileStream, String fileExtName);

    /**
     * 根据文件名从dfs下载文件
     * @param fileName 下载文件名
     * @return
     */
    public NoteResult downloadFile(String fileName);

    /**
     * 根据文件名从dfs删除文件
     * @param fileName 删除文件名
     * @return
     */
    public NoteResult deleteFile(String fileName);
}
