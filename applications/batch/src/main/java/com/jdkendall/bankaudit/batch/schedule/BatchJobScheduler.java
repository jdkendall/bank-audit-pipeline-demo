package com.jdkendall.bankaudit.batch.schedule;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.scheduler.Scheduled;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.JobExecution;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.File;
import java.util.Properties;

@ApplicationScoped
public class BatchJobScheduler {
    private static final Logger LOG = Logger.getLogger(BatchJobScheduler.class);

    @ConfigProperty(name = "parse.directory.path")
    String DIRECTORY_PATH;

    @WithSpan
    @Scheduled(every = "5m")
    void triggerBatchJob() {
        File directory = new File(DIRECTORY_PATH);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                startFileJob(file);
            }
        }
    }

    @WithSpan
    void startFileJob(File file) {
        JobOperator jobOperator = BatchRuntime.getJobOperator();
        Properties parameters = new Properties();
        String filePath = file.getAbsolutePath();
        LOG.info("Setting batch property parameter filePath to '%s'".formatted(filePath));
        parameters.setProperty("filePath", filePath);

        long executionId = jobOperator.start("fileProcessingJob", parameters);
        LOG.info("Batch job started with execution ID: %d for file %s".formatted(executionId, filePath));
    }
}
