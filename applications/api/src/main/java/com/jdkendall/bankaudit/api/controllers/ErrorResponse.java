package com.jdkendall.bankaudit.api.controllers;

import java.util.List;

public record ErrorResponse(String validationError, List<String> errors) {
}
