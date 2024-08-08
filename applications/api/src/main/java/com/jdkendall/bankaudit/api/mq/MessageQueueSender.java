package com.jdkendall.bankaudit.api.mq;

import com.jdkendall.bankaudit.domain.AuditRequest;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageQueueSender {
    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.queue.pending.all}")
    private String routingKey;

    public MessageQueueSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @WithSpan
    public void send(AuditRequest auditRequest) {
        rabbitTemplate.convertAndSend(exchange, routingKey, auditRequest, message -> {
            message.getMessageProperties().setHeader("sender", "api");
            return message;
        });
    }
}
