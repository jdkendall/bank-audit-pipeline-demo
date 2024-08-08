package com.jdkendall.bankaudit.batch.config;

import com.jdkendall.bankaudit.batch.services.mft.FileDataStore;
import com.jdkendall.bankaudit.batch.services.mft.LocalFileDataStore;
import com.jdkendall.bankaudit.batch.services.mft.S3DataStore;
import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import io.awspring.cloud.s3.S3PathMatchingResourcePatternResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class FileSourceConfig {
    @Configuration
    @Profile("default")
    @EnableAutoConfiguration(exclude = {S3AutoConfiguration.class} )
    static class DefaultFileDataStoreConfig {
        @Bean
        FileDataStore fileDataStore(@Value("${parse.local.pattern}") String pattern) throws IOException {
            Path directory = Files.createTempDirectory("bankaudit");
            return new LocalFileDataStore(directory, pattern);
        }

        @Bean
        ResourcePatternResolver resourcePatternResolver(ApplicationContext applicationContext) {
            return new PathMatchingResourcePatternResolver(applicationContext);
        }
    }

    @Configuration
    @Profile("s3")
    static class S3DataStoreConfig {
        @Bean
        FileDataStore fileDataStore(@Value("${parse.s3.bucket}") String s3BucketUrl, @Value("${parse.s3.pattern}") String parsePattern, S3Client s3Client, ResourcePatternResolver resourcePatternResolver) throws IOException {
            return new S3DataStore(s3BucketUrl, parsePattern, s3Client, resourcePatternResolver);
        }

        @Bean
        ResourcePatternResolver resourcePatternResolver(S3Client s3Client, ApplicationContext applicationContext) {
            return new S3PathMatchingResourcePatternResolver(s3Client, applicationContext);
        }
    }
}
