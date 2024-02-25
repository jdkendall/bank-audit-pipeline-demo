package com.jdkendall.bankaudit.batch.batchlets;

import com.jdkendall.bankaudit.batch.domain.AuditLine;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import jakarta.batch.api.AbstractBatchlet;
import jakarta.batch.runtime.context.JobContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import java.util.Map;

@Named
public class AuditingBatchlet extends AbstractBatchlet {

    @Inject
    JobContext jobContext;

    @Inject
    @Channel("pending")
    Emitter<AuditLine> pendingAuditEmitter;

    @Override
    public String process() throws Exception {
        BatchProcessingContext context = (BatchProcessingContext) jobContext.getTransientUserData();
        if (context == null) {
            return "FAILED_NO_CONTEXT";
        }

        context.linesToAudit().forEach(line -> emit(context.filename(), line));

        return "OK";
    }

    private void emit(String filename, AuditLine auditLine) {
        final OutgoingRabbitMQMetadata metadata = new OutgoingRabbitMQMetadata.Builder()
                .withHeader("sender", "batch")
                .withHeader("filename", filename)
                .build();

        Message<AuditLine> message = Message.of(auditLine, Metadata.of(metadata));
        pendingAuditEmitter.send(message);
    }
}
