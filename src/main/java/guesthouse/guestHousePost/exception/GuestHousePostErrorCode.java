package guesthouse.guestHousePost.exception;

import guesthouse.common.exception.ErrorCode;

public enum GuestHousePostErrorCode implements ErrorCode {

    NOT_FOUND("게스트하우스 게시글이 존재하지 않습니다.", 404),
    ;

    private final String message;
    private final int statusCode;

    GuestHousePostErrorCode(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    GuestHousePostErrorCode(String message) {
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