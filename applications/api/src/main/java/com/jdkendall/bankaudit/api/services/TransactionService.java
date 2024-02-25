package com.jdkendall.bankaudit.api.services;

import com.jdkendall.bankaudit.api.domain.TransactionRequest;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.UUID;

@ApplicationScoped
public class TransactionService {
    private static final Logger LOG = Logger.getLogger(TransactionService.class);

    @Inject
    io.vertx.mutiny.pgclient.PgPool client;

    @WithSpan
    public void save(UUID uuid, TransactionRequest request) {
        LOG.info("Saving transaction for uuid: " + uuid);
        Tuple params = Tuple.of(uuid.toString(),
                                request.timestamp(),
                                request.total());
        client.preparedQuery("INSERT INTO transactions (uuid, timestamp, total) " +
                                 "VALUES ($1, $2, $3)")
                .execute(params)
                .await()
                .indefinitely();
        LOG.info("Saved transaction for uuid: " + uuid);
    }
}
