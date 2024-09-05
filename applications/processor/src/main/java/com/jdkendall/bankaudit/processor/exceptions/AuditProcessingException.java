package com.jdkendall.bankaudit.processor.exceptions;

public class AuditProcessingException extends RuntimeException {
    public AuditProcessingException(String message) {
        super(message);
    }
}
