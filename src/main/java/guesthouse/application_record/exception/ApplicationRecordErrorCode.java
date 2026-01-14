package guesthouse.application_record.exception;

import guesthouse.common.exception.ErrorCode;

public enum ApplicationRecordErrorCode implements ErrorCode {

    NOT_ALLOWED("권한이 없습니다."),
    ;

    private final String message;
    private final int statusCode;

    ApplicationRecordErrorCode(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    ApplicationRecordErrorCode(String message) {
        this(message, 400);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return statusCode;
    }

    @Override
    public String getErrorCode() {
        return this.name();
    }
}

