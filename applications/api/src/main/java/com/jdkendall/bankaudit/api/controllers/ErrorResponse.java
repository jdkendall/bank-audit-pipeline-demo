package com.jdkendall.bankaudit.api.controllers;

import java.util.List;

public record ErrorResponse(String error, List<String> errors) {
}
