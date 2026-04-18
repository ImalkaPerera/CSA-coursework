package com.smartcampus.exception;

// Consistent JSON error structure used by all exception mappers
public class ErrorResponse {
    private final String error;
    private final String reason;
    private final String detail;

    public ErrorResponse(String error, String reason) {
        this(error, reason, null);
    }

    public ErrorResponse(String error, String reason, String detail) {
        this.error = error;
        this.reason = reason;
        this.detail = detail;
    }

    public String getError() { return error; }
    public String getReason() { return reason; }
    public String getDetail() { return detail; }
}