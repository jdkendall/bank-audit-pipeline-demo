package com.jdkendall.bankaudit.batch.config;

import com.jdkendall.bankaudit.batch.domain.ParsedLine;
import com.jdkendall.bankaudit.batch.services.MarkCompleteTasklet;
import com.jdkendall.bankaudit.batch.services.parsing.BatchFileProcessor;
import com.jdkendall.bankaudit.batch.services.parsing.BatchFileReader;
import com.jdkendall.bankaudit.domain.AuditRequest;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableScheduling
public class BatchJobConfig extends DefaultBatchConfiguration {
    @Bean
    ItemProcessor<ParsedLine, AuditRequest> batchFileProcessor() {
        return new BatchFileProcessor();
    }

    @Bean
    @StepScope
    BatchFileReader batchFileReader(@Value("#{jobParameters['inputFile']}") Resource file) {
        BatchFileReader batchFileReader = new BatchFileReader();
        batchFileReader.setResource(file);
        return batchFileReader;
    }

    @Bean
    @StepScope
    MarkCompleteTasklet markCompleteTasklet(@Value("#{jobParameters['inputFile']}") Resource file) {
        return new MarkCompleteTasklet(file);
    }

    @Bean
    public Job fileProcessingJob(JobRepository jobRepo,
                                 Step parseFileStep,
                                 Step markCompleteStep) {
        return new JobBuilder("fileProcessingJob", jobRepo)
                .start(parseFileStep)
                .next(markCompleteStep)
                .build();
    }

    @Bean
    public Step markCompleteStep(JobRepository jobRepo,
                                 PlatformTransactionManager txManager,
                                 MarkCompleteTasklet markCompleteTasklet) {
        return new StepBuilder("markCompleteStep", jobRepo)
                .tasklet(markCompleteTasklet, txManager)
                .build();
    }

    @Bean
    public Step parseFileStep(JobRepository jobRepo,
                              PlatformTransactionManager txManager,
                              ItemReader<ParsedLine> batchFileReader,
                              ItemProcessor<ParsedLine, AuditRequest> batchFileProcessor,
                              ItemWriter<AuditRequest> batchAuditWriter) {
        return new StepBuilder("parseFileStep", jobRepo)
                .<ParsedLine, AuditRequest>chunk(300, txManager)
                .reader(batchFileReader)
                .processor(batchFileProcessor)
                .writer(batchAuditWriter)
                .build();
    }

}
