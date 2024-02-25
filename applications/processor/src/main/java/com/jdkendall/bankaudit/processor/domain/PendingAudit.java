package com.jdkendall.bankaudit.processor.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record PendingAudit(UUID uuid, LocalDateTime timestamp, long total) {
}
