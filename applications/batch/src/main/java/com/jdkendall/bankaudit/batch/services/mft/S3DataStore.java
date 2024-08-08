package com.jdkendall.bankaudit.batch.services.mft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Tag;

import java.io.IOException;

public class S3DataStore implements FileDataStore {

    private final String s3Bucket;
    private final S3Client s3Client;
    private final String path;
    private final ResourcePatternResolver resolver;

    public S3DataStore(String s3Bucket, String parsePattern, S3Client s3Client, ResourcePatternResolver resolver) {
        this.s3Bucket = s3Bucket;
        this.s3Client = s3Client;
        this.path = "s3://%s/%s".formatted(s3Bucket, parsePattern);
        this.resolver = resolver;
    }

    @Override
    public void save(String filename, String body) {
        LOG.info("Storing file {} on S3 bucket {}", filename, s3Bucket);
        s3Client.putObject(b -> b.bucket(s3Bucket).key(filename).build(), RequestBody.fromString(body));
    }

    @Override
    public Resource[] getFiles() throws IOException {
        return this.resolver.getResources(this.path);
    }

    @Override
    public void markProcessed(String filename) {
        s3Client.putObjectTagging(b -> b.bucket(s3Bucket)
                .key(filename)
                .tagging(c -> c.tagSet(Tag.builder()
                        .key("processed")
                        .value("true")
                        .build()))
                .build());
    }

    private static final Logger LOG = LoggerFactory.getLogger(S3DataStore.class);
}
