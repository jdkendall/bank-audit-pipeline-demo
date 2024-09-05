package com.jdkendall.bankaudit.batch.services;

import com.jdkendall.bankaudit.domain.Account;
import com.jdkendall.bankaudit.domain.AuditRequest;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class TransactionTrackingService {

    private final DataSource client;

    public TransactionTrackingService(DataSource client) {
        this.client = client;
    }

    @WithSpan
    public void execute(@SpanAttribute String filename, List<? extends AuditRequest> linesToAudit) throws Exception {
        try (var conn = client.getConnection()) {
            try (var stmt = conn.prepareStatement(
                    "insert into transactions (uuid, timestamp, total, src_account, dst_account) values (?, ?, ?, ?, ?)")) {
                for (var line : linesToAudit) {
                    Span.current().addEvent("Saving transaction", Attributes.of(
                            AttributeKey.stringKey("filename"),
                            filename,
                            AttributeKey.stringKey("src-account"),
                            line.sourceAccount().id(),
                            AttributeKey.stringKey("tgt-account"),
                            line.targetAccount().id()
                    ));
                    Integer srcAccountId, tgtAccountId;
                    try(var qry = conn.prepareStatement("select id from accounts where account_num = ? and routing_num = ?")) {
                        srcAccountId = getAccountId(qry, line.sourceAccount());
                        tgtAccountId = getAccountId(qry, line.targetAccount());
                    }

                    if(!validate(line, srcAccountId, tgtAccountId)) {
                        continue;
                    }

                    LOG.debug("Adding transaction for line: {}", line);
                    stmt.setObject(1, line.uuid());
                    stmt.setObject(2, line.timestamp());
                    stmt.setLong(3, line.total());
                    stmt.setInt(4, srcAccountId);
                    stmt.setInt(5, tgtAccountId);
                    stmt.addBatch();
                }
                stmt.execute();
                LOG.info("Saved transactions for file {}", filename);
            }
        }
    }

    private static boolean validate(AuditRequest line, Integer srcAccountId, Integer tgtAccountId) {
        boolean isValid = true;

        LOG.debug("Validating line: {}", line);

        if(line.uuid() == null) {
            LOG.warn("Could not find uuid for line: {}", line);
            isValid = false;
        }

        if(line.timestamp() == null) {
            LOG.warn("Could not find timestamp for line: {}", line);
            isValid = false;
        }

        if(srcAccountId == null) {
            LOG.warn("Could not find source account for line: {}", line);
            isValid = false;
        }

        if(tgtAccountId == null) {
            LOG.warn("Could not find target account for line: {}", line);
            isValid = false;
        }

        return isValid;
    }

    private static Integer getAccountId(PreparedStatement qry, Account account) throws SQLException {
        qry.setString(1, account.id());
        qry.setString(2, account.routingNumber());
        ResultSet results = qry.executeQuery();
        if (results.next()) {
             return results.getInt("id");
        }
        return null;
    }

    private static final Logger LOG = LoggerFactory.getLogger(TransactionTrackingService.class);
}
