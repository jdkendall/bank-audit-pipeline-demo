package com.jdkendall.bankaudit.batch.services.mft;

import org.springframework.core.io.Resource;

import java.io.IOException;

public interface FileDataStore {
    void save(String filename, String body);

    Resource[] getFiles() throws IOException;

    void markProcessed(String filename);
}
