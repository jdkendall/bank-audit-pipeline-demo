package com.jdkendall.bankaudit.batch.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdkendall.bankaudit.domain.AuditRequest;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditingService {
    private final ObjectMapper mapper;

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.queue.pending.all}")
    private String routingKey;

    public AuditingService(ObjectMapper mapper, RabbitTemplate rabbitTemplate) {
        this.mapper = mapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @WithSpan
    public void execute(String filename, List<? extends AuditRequest> linesToAudit) throws Exception {
        for (AuditRequest line : linesToAudit) {
            LOG.debug("Emitting audit line: {}", line);
            emit(filename, line);
        }
        LOG.info("Emitted {} audit lines for file {}", linesToAudit.size(), filename);
    }

    @WithSpan
    private void emit(@SpanAttribute String filename, AuditRequest req) throws JsonProcessingException {
        Span.current()
                .setAttribute("uuid", req.uuid().toString())
                .setAttribute("src-account-num", req.sourceAccount().id())
                .setAttribute("src-routing-num", req.sourceAccount().routingNumber())
                .setAttribute("tgt-account-num", req.targetAccount().id())
                .setAttribute("tgt-routing-num", req.targetAccount().routingNumber());

        // Put audit request UUID and account info into baggage
        try(var ignored = Baggage.current().toBuilder()
                .put("origin", "file: [%s]".formatted(filename))
                .put("uuid", req.uuid().toString())
                .build().makeCurrent()
            ) {
            Message message = MessageBuilder.withBody(mapper.writeValueAsBytes(req))
                    .setHeader("sender", "batch")
                    .setHeader("filename", filename)
                    .build();
            rabbitTemplate.setObservationEnabled(true);
            rabbitTemplate.send(exchange, routingKey, message);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AuditingService.class);
}
