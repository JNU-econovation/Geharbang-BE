package guesthouse.notification.exception;

import guesthouse.common.exception.ErrorCode;

public enum NotificationErrorCode implements ErrorCode {

    NOT_FOUND("알림이 존재하지 않습니다.", 404),
    ;

    private final String message;
    private final int statusCode;

    NotificationErrorCode(String message, int statusCode) {
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
