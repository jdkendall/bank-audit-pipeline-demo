package com.jdkendall.bankaudit.api.exceptions;

import java.util.List;

public class ApiValidationException extends RuntimeException {
    private final List<String> errors;

    public ApiValidationException(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
