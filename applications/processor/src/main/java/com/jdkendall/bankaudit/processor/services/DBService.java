package com.jdkendall.bankaudit.processor.services;

import com.jdkendall.bankaudit.processor.domain.ProcessedAudit;
import com.jdkendall.bankaudit.processor.domain.SenderType;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DBService {
    @Inject
    io.vertx.mutiny.pgclient.PgPool client;

    public void save(SenderType sender, ProcessedAudit audit) {
        Tuple params = Tuple.of(audit.uuid().toString(), sender.name(), audit.flagged());
        client.preparedQuery("INSERT INTO audits (uuid, source, flagged, audit_timestamp) " +
                                 "VALUES ($1, $2, $3, CURRENT_TIMESTAMP)")
                .execute(params)
                .await()
                .indefinitely();
    }
}
