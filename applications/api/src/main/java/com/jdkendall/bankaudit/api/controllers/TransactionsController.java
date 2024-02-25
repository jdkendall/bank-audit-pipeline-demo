package com.jdkendall.bankaudit.api.controllers;

import com.jdkendall.bankaudit.api.domain.AuditRequest;
import com.jdkendall.bankaudit.api.domain.TransactionRequest;
import com.jdkendall.bankaudit.api.mq.MessageQueueSender;
import com.jdkendall.bankaudit.api.services.TransactionService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ResponseStatus;

import java.util.UUID;

@Path("/transactions")
public class TransactionsController {
    private static final Logger LOG = Logger.getLogger(TransactionsController.class);

    @Inject
    MessageQueueSender mqSender;

    @Inject
    TransactionService txService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ResponseStatus(204)
    @WithSpan
    public void submitTransaction(TransactionRequest request) {
        if(request.total() == null || request.timestamp() == null) {
            throw new RuntimeException("Missing total and/or timestamp fields.");
        }

        UUID uuid = UUID.randomUUID();
        txService.save(uuid, request);

        LOG.info("Sending audit request for uuid: " + uuid);
        AuditRequest auditRequest = new AuditRequest(uuid, request.timestamp(), request.total());
        mqSender.send(auditRequest);
        LOG.info("Sent audit request for uuid: " + uuid);
    }
}
