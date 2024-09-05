package com.jdkendall.bankaudit.batch.schedule;

import com.jdkendall.bankaudit.batch.services.mft.FileDataStore;
import io.awspring.cloud.s3.Location;
import io.awspring.cloud.s3.S3Resource;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Component
public class BatchJobScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(BatchJobScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job fileProcessingJob;
    private final FileDataStore fileDataStore;

    public BatchJobScheduler(JobLauncher jobLauncher, Job fileProcessingJob, FileDataStore fileDataStore) {
        this.jobLauncher = jobLauncher;
        this.fileProcessingJob = fileProcessingJob;
        this.fileDataStore = fileDataStore;
    }

    @Scheduled(fixedRateString = "${scan.rate:3600000}")
    public void triggerBatchJob() throws IOException {
        LOG.info("Checking for new files");

        Resource[] files = this.fileDataStore.getFiles();

        for (Resource file : files) {
            startFileJob(file);
        }

    }

    private void startFileJob(Resource file) throws IOException {
        try {
            JobParametersBuilder jobParams = new JobParametersBuilder();
            if (file instanceof S3Resource s3File) {
                Location location = s3File.getLocation();
                jobParams
                        .addString("type", "s3", false)
                        .addString("inputFile", "s3://%s/%s".formatted(location.getBucket(), location.getObject()), true)
                        .addString("bucket", location.getBucket(), false)
                        .addString("filename", location.getObject(), false);
            } else {
                jobParams
                        .addString("type", "local", false)
                        .addString("inputFile", file.getURL().toString(), true)
                        .addString("filename", Objects.requireNonNull(file.getFilename()), false);
            }
            jobLauncher.run(fileProcessingJob, jobParams.toJobParameters());
            LOG.info("Batch job started for file {}", file.getURL());
        } catch (JobInstanceAlreadyCompleteException ex) {
            LOG.error("File {} already processed, marking complete.", file.getURL());
            this.fileDataStore.markProcessed(file.getFilename());
        } catch (Exception ex) {
            LOG.error("Failed to start batch job for file {}", file.getURL(), ex);
        }
    }
}
