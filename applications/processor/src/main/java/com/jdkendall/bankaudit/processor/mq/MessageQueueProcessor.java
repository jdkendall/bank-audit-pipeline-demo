package com.jdkendall.bankaudit.processor.mq;

import com.jdkendall.bankaudit.processor.domain.PendingAudit;
import com.jdkendall.bankaudit.processor.domain.ProcessedAudit;
import com.jdkendall.bankaudit.processor.domain.SenderType;
import com.jdkendall.bankaudit.processor.services.AuditService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMetadata;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Named
public class MessageQueueProcessor {
    private static final Logger LOG = Logger.getLogger(MessageQueueProcessor.class);

    @Inject
    @Channel("processedBatch")
    Emitter<ProcessedAudit> processedAuditBatchEmitter;

    @Inject
    @Channel("processedApi")
    Emitter<ProcessedAudit> processedAuditApiEmitter;

    @Inject
    AuditService auditService;


    @Blocking
    @Incoming("pending")
    public CompletionStage<Void> consume(Message<JsonObject> auditMessage) {
        IncomingRabbitMQMetadata metadata = auditMessage.getMetadata(IncomingRabbitMQMetadata.class).orElse(null);
        PendingAudit payload = auditMessage.getPayload().mapTo(PendingAudit.class);
        if (metadata != null) {
            String headers = metadata.getHeaders().entrySet().stream()
                    .map(e -> "%s => %s".formatted(e.getKey(), e.getValue()))
                    .collect(Collectors.joining("\n"));
            LOG.info("Received message: %s with headers:\n%s".formatted(auditMessage.getPayload(), headers));

            SenderType sender = parseSender(metadata);

            ProcessedAudit processedAudit = this.auditService.audit(sender, payload);

            switch (sender) {
                case BATCH:
                    processedAuditBatchEmitter.send(processedAudit);
                    break;
                case API:
                    processedAuditApiEmitter.send(processedAudit);
                    break;
                default:
                    LOG.info("Unknown sender type: " + sender);
            }
        }

        return auditMessage.ack();
    }

    private SenderType parseSender(IncomingRabbitMQMetadata metadata) {
        String senderHeader = metadata.getHeader("sender", String.class).orElse("");
        try {
            return SenderType.valueOf(senderHeader.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Proper architecture would have a queue for anomalous messages so nothing is lost,
            // but this is a toy demo, so we're going to panic throw and discard
            // the message instead. :^)

            LOG.info("Unknown sender type! " + senderHeader);
            throw e;
        }
    }
}
