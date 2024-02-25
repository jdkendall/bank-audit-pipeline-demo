package com.jdkendall.bankaudit.batch.batchlets;

import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.batch.api.AbstractBatchlet;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;
import java.util.stream.Collectors;

@Named
@Dependent
public class TransactionTrackingBatchlet extends AbstractBatchlet {

    @Inject
    JobContext jobContext;

    @Inject
    io.vertx.mutiny.pgclient.PgPool client;

    @Override
    public String process() throws Exception {
        BatchProcessingContext context = (BatchProcessingContext) jobContext.getTransientUserData();
        if (context == null) {
            return "FAILED_NO_CONTEXT";
        }

        List<Tuple> params = context.linesToAudit().stream()
                .map(line -> Tuple.of(line.uuid().toString(),
                        line.timestamp(),
                        line.total())
                ).collect(Collectors.toList());
        client.preparedQuery("INSERT INTO transactions (uuid, timestamp, total) " +
                        "VALUES ($1, $2, $3)")
                .executeBatch(params)
                .await()
                .indefinitely();

        return "OK";
    }
}
