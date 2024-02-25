package com.jdkendall.bankaudit.batch.batchlets.parsing;

import com.jdkendall.bankaudit.batch.domain.ParsedLine;
import jakarta.annotation.PostConstruct;
import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Optional;

import static org.jboss.logging.Logger.getLogger;

@Named
@Dependent
@Transactional
public class BatchFileReader extends AbstractItemReader {
    private BufferedReader reader;

    @ConfigProperty(name = "audit.batch.filePath")
    Optional<String> filePath;

    @Override
    public void open(Serializable _checkpoint) throws Exception {
        reader = new BufferedReader(new FileReader(filePath.get()));
    }

    @Override
    public void close() throws Exception {
        if (reader != null) {
            reader.close();
        }
    }

    @Override
    public Object readItem() throws Exception {
        String line = reader.readLine();
        if(line.isBlank()) {
            return null;
        }

        String[] parts = line.split("\\|");
        if (parts.length < 3) {
            throw new RuntimeException("Invalid line: %s (expected 3 parts, got %d)".formatted(line, parts.length));
        }

        return new ParsedLine(parts[0].trim(), parts[2].trim(), parts[1].trim());
    }
}
