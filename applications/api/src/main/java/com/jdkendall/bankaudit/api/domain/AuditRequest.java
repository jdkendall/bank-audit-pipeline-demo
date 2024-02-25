package com.jdkendall.bankaudit.api.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditRequest(UUID uuid, LocalDateTime  timestamp, long total) {
}
