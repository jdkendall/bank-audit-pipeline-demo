package com.jdkendall.bankaudit.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditRequest(UUID uuid, Account sourceAccount, Account targetAccount, LocalDateTime  timestamp, long total) {
}
