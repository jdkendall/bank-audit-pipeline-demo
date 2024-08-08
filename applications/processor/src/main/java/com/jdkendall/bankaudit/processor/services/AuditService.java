package com.jdkendall.bankaudit.processor.services;

import com.jdkendall.bankaudit.domain.AuditRequest;
import com.jdkendall.bankaudit.processor.domain.ProcessedAudit;
import com.jdkendall.bankaudit.processor.domain.SenderType;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class AuditService {
    private final DBService DBService;

    public AuditService(DBService DBService) {
        this.DBService = DBService;
    }

    @WithSpan
    public ProcessedAudit audit(SenderType sender, AuditRequest payload) throws SQLException {
        boolean flagged = switch (sender) {
            // API calls are suspicious if they're over $25.00
            case API -> payload.total() > 25_00;
            // Batch calls are suspicious if they're over $500.00
            case BATCH -> payload.total() > 500_00;
        };

        logProcessingStatusEvent(sender, payload, flagged);

        ProcessedAudit processedAudit = new ProcessedAudit(payload.uuid(), flagged);

        DBService.save(sender, processedAudit);

        return processedAudit;
    }

    private static void logProcessingStatusEvent(SenderType sender, AuditRequest payload, boolean flagged) {
        Span.current()
                .addEvent("Determined flag status of request",
                        Attributes.of(
                                AttributeKey.stringKey("sender"),
                                sender.name(),
                                AttributeKey.stringKey("UUID"),
                                payload.uuid().toString(),
                                AttributeKey.booleanKey("auditStatus"),
                                flagged,
                                AttributeKey.stringKey("origin"),
                                Baggage.current().getEntryValue("origin"))
                );
    }
}
