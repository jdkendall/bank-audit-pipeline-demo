package com.jdkendall.bankaudit.batch.batchlets.parsing;

import com.jdkendall.bankaudit.batch.domain.AuditLine;
import com.jdkendall.bankaudit.batch.domain.ParsedLine;
import jakarta.batch.api.chunk.ItemProcessor;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Named
@Dependent
public class BatchFileProcessor implements ItemProcessor {
    @Override
    public Object processItem(Object in) throws Exception {
        ParsedLine line = (ParsedLine) in;

        UUID uuid = UUID.fromString(line.uuid());
        LocalDateTime timestamp = LocalDateTime.parse(line.timestamp(), DateTimeFormatter.ISO_DATE_TIME);
        long total = Long.parseLong(line.total());

        return new AuditLine(uuid, timestamp, total);
    }
}
