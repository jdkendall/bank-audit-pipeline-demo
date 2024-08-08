package com.jdkendall.bankaudit.processor.services;


import com.jdkendall.bankaudit.processor.domain.ProcessedAudit;
import com.jdkendall.bankaudit.processor.domain.SenderType;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;

@Service
public class DBService {
    private final DataSource client;

    public DBService(DataSource client) {
        this.client = client;
    }

    public void save(SenderType sender, ProcessedAudit audit) throws SQLException {
        try (var conn = client.getConnection();
             var stmt = conn.prepareStatement(
                "INSERT INTO audits (uuid, source, flagged, audit_timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP)"
        )) {
            stmt.setObject(1, audit.uuid());
            stmt.setString(2, sender.name());
            stmt.setBoolean(3, audit.flagged());
            stmt.execute();
        }
    }
}
