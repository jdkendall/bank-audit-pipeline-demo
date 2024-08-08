package com.jdkendall.bankaudit.batch.services.parsing;

import com.jdkendall.bankaudit.batch.domain.ParsedLine;
import com.jdkendall.bankaudit.domain.Account;
import com.jdkendall.bankaudit.domain.AuditRequest;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class BatchFileProcessor implements ItemProcessor<ParsedLine, AuditRequest> {

    @Override
    public AuditRequest process(ParsedLine line) {
        UUID uuid = UUID.fromString(line.uuid());
        LocalDateTime timestamp = LocalDateTime.parse(line.timestamp(), DateTimeFormatter.ISO_DATE_TIME);
        long total = Long.parseLong(line.total());

        return new AuditRequest(uuid,
                new Account(line.srcAccountId(), line.srcAccountRoutingNum()),
                new Account(line.tgtAccountId(), line.tgtAccountRoutingNum()),
                timestamp,
                total);
    }
}
