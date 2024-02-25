package com.jdkendall.bankaudit.batch.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLine(UUID uuid, LocalDateTime timestamp, long total) {
}
