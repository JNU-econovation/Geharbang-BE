package guesthouse.application.exception;

import guesthouse.common.exception.ErrorCode;

public enum ApplicationErrorCode implements ErrorCode {
    NOT_FOUND("지원서를 찾을 수 없습니다.", 404);

    private final String message;
    private final int statusCode;

    ApplicationErrorCode(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
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
