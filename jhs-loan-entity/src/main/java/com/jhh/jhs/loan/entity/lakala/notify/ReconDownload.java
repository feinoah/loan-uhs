package com.jhh.jhs.loan.entity.lakala.notify;

import com.jhh.jhs.loan.entity.lakala.webHook.SuperWebHookRequest;

import java.io.InputStream;

public class ReconDownload extends SuperWebHookRequest {

    private InputStream in;

    private String fileName;

    private String privData;

    public InputStream getIn() {
        return in;
    }

    public void setIn(InputStream in) {
        this.in = in;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPrivData() {
        return privData;
    }

    public void setPrivData(String privData) {
        this.privData = privData;
    }
}
