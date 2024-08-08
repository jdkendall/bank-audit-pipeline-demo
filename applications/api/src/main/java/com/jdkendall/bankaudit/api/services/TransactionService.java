package com.jdkendall.bankaudit.api.services;

import com.jdkendall.bankaudit.api.exceptions.ApiValidationException;
import com.jdkendall.bankaudit.domain.Account;
import com.jdkendall.bankaudit.domain.AuditRequest;
import com.jdkendall.bankaudit.domain.TransactionRequest;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionService.class);

    private final DataSource client;

    public TransactionService(DataSource client) {
        this.client = client;
    }

    @WithSpan
    public void save(UUID uuid, TransactionRequest request) throws SQLException {
        LOG.info("Saving transaction for uuid: {}", uuid);
        try (var conn = client.getConnection();
             var stmt = conn.prepareStatement(
                    "insert into transactions (uuid, timestamp, total, src_account, dst_account) values (?, ?, ?, ?, ?)")) {
            Integer srcAccountId, tgtAccountId;
            try(var qry = conn.prepareStatement("select id from accounts where account_num = ? and routing_num = ?")) {
                srcAccountId = getAccountId(qry, request.sourceAccount());
                tgtAccountId = getAccountId(qry, request.destinationAccount());
            }

            List<String> errors = validate(uuid, request, srcAccountId, tgtAccountId);
            if(!errors.isEmpty()) {
                throw new ApiValidationException(errors);
            }

            LOG.debug("Adding transaction for line: {}", request);
            stmt.setObject(1, uuid);
            stmt.setObject(2, request.timestamp());
            stmt.setLong(3, request.total());
            stmt.setInt(4, srcAccountId);
            stmt.setInt(5, tgtAccountId);
            stmt.execute();
            LOG.info("Saved transaction for uuid: {}", uuid);
        }
    }

    private static List<String> validate(UUID uuid, TransactionRequest txReq, Integer srcAccountId, Integer tgtAccountId) {
        var errors = new ArrayList<String>();

        LOG.debug("Validating line: {}", txReq);

        if(uuid == null) {
            LOG.warn("Could not find uuid for txReq: {}", txReq);
            errors.add("`uuid` is required");
        }

        if(txReq.timestamp() == null) {
            LOG.warn("Could not find timestamp for txReq: {}", txReq);
            errors.add("`timestamp` is required");
        }

        if(srcAccountId == null) {
            LOG.warn("Could not find source account for txReq: {}", txReq);
            errors.add("`sourceAccount.id` is invalid");
        }

        if(tgtAccountId == null) {
            LOG.warn("Could not find target account for txReq: {}", txReq);
            errors.add("`destinationAccount.id` is invalid");
        }

        return errors;
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
}
