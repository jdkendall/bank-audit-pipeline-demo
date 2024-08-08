package com.jdkendall.bankaudit.api.controllers;

import com.jdkendall.bankaudit.api.exceptions.ApiValidationException;
import com.jdkendall.bankaudit.api.mq.MessageQueueSender;
import com.jdkendall.bankaudit.api.services.TransactionService;
import com.jdkendall.bankaudit.domain.AuditRequest;
import com.jdkendall.bankaudit.domain.TransactionRequest;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Controller
public class TransactionsController {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionsController.class);

    private final MessageQueueSender mqSender;

    private final TransactionService txService;

    public TransactionsController(MessageQueueSender mqSender, TransactionService txService) {
        this.mqSender = mqSender;
        this.txService = txService;
    }

    // POST route which takes application/json and returns a 204
    @WithSpan
    @PostMapping(path = "/transactions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> submitTransaction(@RequestBody TransactionRequest request) throws SQLException {
        var errors = request.validate();
        if (!errors.isEmpty()) {
            throw new ApiValidationException(errors);
        }

        UUID uuid = UUID.randomUUID();
        txService.save(uuid, request);

        LOG.info("Sending audit request for uuid: {}", uuid);
        AuditRequest auditRequest = new AuditRequest(
                uuid,
                request.sourceAccount(),
                request.destinationAccount(),
                request.timestamp(),
                request.total());
        mqSender.send(auditRequest);
        LOG.info("Sent audit request for uuid: {}", uuid);

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ApiValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ApiValidationException e) {
        LOG.error("Validation error: {}", e.getErrors());
        return ResponseEntity.badRequest().body(new ErrorResponse("Validation error", e.getErrors()));
    }

    @ExceptionHandler()
    public ResponseEntity<ErrorResponse> handleOtherExceptions(Throwable e) {
        LOG.error("Unknown error", e);
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), List.of()));
    }
}
