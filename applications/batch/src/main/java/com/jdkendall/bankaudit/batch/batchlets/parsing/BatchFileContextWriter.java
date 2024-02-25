package com.jdkendall.bankaudit.batch.batchlets.parsing;

import com.jdkendall.bankaudit.batch.batchlets.BatchProcessingContext;
import com.jdkendall.bankaudit.batch.domain.AuditLine;
import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Named
@Dependent
@Transactional
public class BatchFileContextWriter extends AbstractItemWriter {

    @Inject
    JobContext jobContext;

    @ConfigProperty(name = "audit.batch.filePath")
    Optional<String> filePath;

    @Override
    public void writeItems(List<Object> list) throws Exception {
        List<AuditLine> lines = new ArrayList<>();

        for (Object item : list) {
            lines.add((AuditLine) item);
        }

        BatchProcessingContext context = (BatchProcessingContext) jobContext.getTransientUserData();
        if(context == null) {
            context = new BatchProcessingContext(filePath.get(), lines);
            jobContext.setTransientUserData(context);
        } else {
            context.linesToAudit().clear();
            context.linesToAudit().addAll(lines);
        }
    }
}
