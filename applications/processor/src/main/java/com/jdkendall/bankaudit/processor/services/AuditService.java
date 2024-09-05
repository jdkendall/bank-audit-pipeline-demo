package com.jdkendall.bankaudit.processor.services;

import com.jdkendall.bankaudit.domain.AuditRequest;
import com.jdkendall.bankaudit.processor.domain.ProcessedAudit;
import com.jdkendall.bankaudit.processor.domain.SenderType;
import com.jdkendall.bankaudit.processor.exceptions.AuditProcessingException;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
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
    public ProcessedAudit audit(@SpanAttribute SenderType sender, AuditRequest payload) throws SQLException {
        Span.current()
                .setAttribute("src-account", payload.sourceAccount().toString())
                .setAttribute("tgt-account", payload.targetAccount().toString());

        boolean flagged = switch (sender) {
            // API calls are suspicious if they're over $25.00
            case API -> payload.total() > 25_00;
            // Batch calls are suspicious if they're over $500.00
            case BATCH -> payload.total() > 500_00;
        };

        if("123456789".equals(payload.targetAccount().id()) && "987654321".equals(payload.targetAccount().routingNumber()) && (Math.random() * 100) < 2) {
            throw new AuditProcessingException("Simulated failure due to issue with account");
        }

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
