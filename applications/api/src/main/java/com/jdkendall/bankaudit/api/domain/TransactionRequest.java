package com.jdkendall.bankaudit.api.domain;

import java.time.LocalDateTime;

public record TransactionRequest(LocalDateTime timestamp, Long total) {
}
