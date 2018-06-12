package com.jhh.service.dfs.controller;

import com.jhh.jhs.loan.common.util.CodeReturn;
import com.jhh.service.dfs.service.FdfsService;
import com.jhh.jhs.loan.entity.app.NoteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * FDFS文件服务HTTP接口
 * @author chenchao
 */
@Controller
@RequestMapping(value = "fdfs")
@Slf4j
public class FdfsController {

    @Autowired
    private FdfsService fdfsService;

    @RequestMapping(value = "uploadFile", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody
    NoteResult uploadFile(@RequestParam(value = "file", required = true) MultipartFile file, @RequestParam(required = false) String fileExtName) {
        NoteResult result = new NoteResult();
        try {
            return fdfsService.uploadFile(file.getInputStream(), fileExtName);
        } catch (IOException e) {
            log.error("上传文件到DFS发生异常", e);
            result.setCode(String.valueOf(CodeReturn.fail));
            result.setInfo("上传文件到DFS发生异常");
        }
        return result;
    }

    @RequestMapping(value = "downloadFile", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody
    NoteResult downloadFile(String fileName) {
        return fdfsService.downloadFile(fileName);
    }

    @RequestMapping(value = "deleteFile", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody
    NoteResult deleteFile(String fileName) {
        return fdfsService.deleteFile(fileName);
    }
}
