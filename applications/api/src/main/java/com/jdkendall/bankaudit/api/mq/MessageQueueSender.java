package com.jdkendall.bankaudit.api.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdkendall.bankaudit.domain.AuditRequest;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageQueueSender {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.queue.pending.all}")
    private String routingKey;

    public MessageQueueSender(RabbitTemplate rabbitTemplate, ObjectMapper mapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
    }

    @WithSpan
    public void send(AuditRequest auditRequest) throws JsonProcessingException {
        rabbitTemplate.convertAndSend(exchange, routingKey, mapper.writeValueAsBytes(auditRequest), message -> {
            message.getMessageProperties().setHeader("sender", "api");
            return message;
        });
    }
}
