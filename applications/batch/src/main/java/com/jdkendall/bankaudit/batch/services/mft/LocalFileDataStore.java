package com.jdkendall.bankaudit.batch.services.mft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

public class LocalFileDataStore implements FileDataStore {
    private final Path localPath;
    private final String pattern;

    public LocalFileDataStore(Path localPath, String pattern) {
        this.localPath = localPath;
        this.pattern = pattern;
    }

    @Override
    public void save(String filename, String body) {
        LOG.info("Storing file {} to local file system at {}", filename, localPath);

        try {
            writeFile(filename, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resource[] getFiles() throws IOException {
        try (var pathStream = Files.newDirectoryStream(localPath, pattern)) {
            return StreamSupport.stream(pathStream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .map(FileSystemResource::new)
                    .toArray(Resource[]::new);
        }
    }

    @Override
    public void markProcessed(String filename) {
        try {
            Files.move(localPath.resolve(filename), localPath.resolve(filename + ".complete"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeFile(String filename, String body) throws IOException {
        Files.writeString(localPath.resolve(filename), body);
    }

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileDataStore.class);
}
