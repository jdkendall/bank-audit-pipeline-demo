package com.jdkendall.bankaudit.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record TransactionRequest(Account sourceAccount, Account destinationAccount, LocalDateTime timestamp, Long total) {
    public List<String> validate() {
        var errors = new ArrayList<String>();
        if (sourceAccount == null) {
            errors.add("`sourceAccount` is required");
        }
        if (destinationAccount == null) {
            errors.add("`destinationAccount` is required");
        }
        if (timestamp == null) {
            errors.add("`timestamp` is required");
        }
        if (total == null) {
            errors.add("`total` is required");
        }
        return errors;
    }
}
