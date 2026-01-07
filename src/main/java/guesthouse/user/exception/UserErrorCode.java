package guesthouse.user.exception;

import guesthouse.common.exception.ErrorCode;

public enum UserErrorCode implements ErrorCode {
    NOT_FOUND("유저 정보를 찾을수 없습니다.", 404);

    private final String message;
    private final int statusCode;

    UserErrorCode(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    UserErrorCode(String message) {
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
