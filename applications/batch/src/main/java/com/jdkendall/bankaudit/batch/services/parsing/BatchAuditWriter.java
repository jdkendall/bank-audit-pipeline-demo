package com.jdkendall.bankaudit.batch.services.parsing;

import com.jdkendall.bankaudit.batch.services.AuditingService;
import com.jdkendall.bankaudit.batch.services.TransactionTrackingService;
import com.jdkendall.bankaudit.batch.services.mft.FileDataStore;
import com.jdkendall.bankaudit.domain.AuditRequest;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class BatchAuditWriter implements ItemWriter<AuditRequest> {
    private final AuditingService auditingService;
    private final TransactionTrackingService transactionTrackingService;
    private final FileDataStore fileDataStore;

    @Value("#{jobParameters['bucket']}")
    private String bucket;

    @Value("#{jobParameters['filename']}")
    private String filename;

    @Value("#{jobParameters['type']}")
    private String type;

    public BatchAuditWriter(AuditingService auditingService, TransactionTrackingService transactionTrackingService, FileDataStore fileDataStore) {
        this.auditingService = auditingService;
        this.transactionTrackingService = transactionTrackingService;
        this.fileDataStore = fileDataStore;
    }

    @Override
    public void write(Chunk<? extends AuditRequest> chunk) throws Exception {
        auditingService.execute(filename, chunk.getItems());
        transactionTrackingService.execute(filename, chunk.getItems());
    }
}
