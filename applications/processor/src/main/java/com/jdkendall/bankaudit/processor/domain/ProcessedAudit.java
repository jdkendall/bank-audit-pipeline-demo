package com.jdkendall.bankaudit.processor.domain;

import java.util.UUID;

public record ProcessedAudit(UUID uuid, boolean flagged) {
}
