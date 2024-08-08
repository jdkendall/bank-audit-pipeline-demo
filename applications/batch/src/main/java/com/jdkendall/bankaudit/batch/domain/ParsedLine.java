package com.jdkendall.bankaudit.batch.domain;

public record ParsedLine(String uuid, String srcAccountId, String srcAccountRoutingNum, String tgtAccountId, String tgtAccountRoutingNum, String total, String timestamp) {}
