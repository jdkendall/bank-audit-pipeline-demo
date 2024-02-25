package com.jdkendall.bankaudit.batch.batchlets;

import com.jdkendall.bankaudit.batch.domain.AuditLine;

import java.util.List;

public record BatchProcessingContext(String filename, List<AuditLine> linesToAudit) {
}
