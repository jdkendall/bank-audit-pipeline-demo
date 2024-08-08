package com.jdkendall.bankaudit.processor.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdkendall.bankaudit.domain.AuditRequest;
import com.jdkendall.bankaudit.processor.domain.ProcessedAudit;
import com.jdkendall.bankaudit.processor.domain.SenderType;
import com.jdkendall.bankaudit.processor.services.AuditService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;

@Component
public class MessageQueueProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageQueueProcessor.class);

    private final RabbitTemplate rabbitTemplate;
    private final AuditService auditService;

    @Value("${spring.rabbitmq.exchange}")
    private String processedExchange;

    @Value("${spring.rabbitmq.queue.processed.batch}")
    private String processedBatchRoutingKey;

    @Value("${spring.rabbitmq.queue.processed.api}")
    private String processedApiRoutingKey;

    private final ObjectMapper objectMapper;


    public MessageQueueProcessor(RabbitTemplate rabbitTemplate, AuditService auditService, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @WithSpan
    @RabbitListener(queues = "${spring.rabbitmq.queue.pending.all}")
    public void consume(Message auditMessage, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
                        @Header("sender") String senderHeader) throws IOException, SQLException {
        AuditRequest payload = objectMapper.readValue(auditMessage.getBody(), AuditRequest.class);
        SenderType sender = parseSender(senderHeader);

        LOG.info("Queue [{}]: Received message with UUID: [{}] and sender: [{}]", routingKey, payload.uuid(), sender);

        ProcessedAudit processedAudit = this.auditService.audit(sender, payload);

        switch (sender) {
            case BATCH:
                rabbitTemplate.convertAndSend(processedExchange, processedBatchRoutingKey, objectMapper.writeValueAsString(processedAudit));
                break;
            case API:
                rabbitTemplate.convertAndSend(processedExchange, processedApiRoutingKey, objectMapper.writeValueAsString(processedAudit));
                break;
            default:
                LOG.info("Unknown sender type: {}", sender);
        }
    }

    private SenderType parseSender(String senderHeader) {
        try {
            return SenderType.valueOf(senderHeader.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOG.info("Unknown sender type! {}", senderHeader);
            throw e;
        }
    }
}
