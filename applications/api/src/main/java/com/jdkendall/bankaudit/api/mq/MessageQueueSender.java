package com.jdkendall.bankaudit.api.mq;

import com.jdkendall.bankaudit.api.domain.AuditRequest;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

@ApplicationScoped
public class MessageQueueSender {

    @Inject
    @Channel("pending")
    Emitter<AuditRequest> pendingAuditEmitter;

    @WithSpan
    public void send(AuditRequest auditRequest) {
        final OutgoingRabbitMQMetadata metadata = new OutgoingRabbitMQMetadata.Builder()
                .withHeader("sender", "api")
                .build();

        Message<AuditRequest> message = Message.of(auditRequest, Metadata.of(metadata));
        pendingAuditEmitter.send(message);
    }
}
