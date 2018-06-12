package com.jhh.service.dfs.test;

import com.jhh.jhs.loan.common.util.CodeReturn;
import com.jhh.service.dfs.service.impl.FdfsServiceImpl;
import com.jhh.jhs.loan.entity.app.NoteResult;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by chenchao on 2018/1/2.
 */
@FixMethodOrder(MethodSorters.JVM)
public class FdfsServiceTest {
    public static final String FILE_CONTENT = "fdfs test";
    private static File testFile = null;
    private static String remoteFileName;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        if (testFile == null) {
            testFile = File.createTempFile("FDFSTest", "txt");
            FileUtils.write(testFile, FILE_CONTENT);
        }
    }

    @AfterClass
    public static void setUpAfterClass() throws Exception {
        if (testFile != null) {
            testFile.delete();
        }
    }

    @Test
    public void testUploadFile() {
        try {
            NoteResult result = new FdfsServiceImpl().uploadFile(new FileInputStream(testFile), "test");
            Assert.assertTrue(result.getCode().equals(String.valueOf(CodeReturn.success)));
            remoteFileName = result.getData().toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDownloadFile() {
        try {
            NoteResult result = new FdfsServiceImpl().downloadFile(remoteFileName);
            Assert.assertTrue(result.getCode().equals(String.valueOf(CodeReturn.success)));
            Assert.assertTrue("fdfs test".equals(new String((byte[]) result.getData())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteFile() {
        try {
            NoteResult result = new FdfsServiceImpl().deleteFile(remoteFileName);
            Assert.assertTrue(result.getCode().equals(String.valueOf(CodeReturn.success)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
