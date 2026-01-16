package guesthouse.user.exception;

import guesthouse.common.exception.ErrorCode;

public enum UserErrorCode implements ErrorCode {
    NOT_FOUND("유저 정보를 찾을수 없습니다.", 404),
    INVALID_GENDER("유효하지 않는 성별입니다."),
    NAME_REQUIRED("유저 이름은 null이거나 비어있을 수 없습니다"),
    PHONE_NUMBER_REQUIRED("휴대폰 번호는 null이거나 비어있을 수 없습니다"),
    BIRTH_DATE_REQUIRED("출생년도는 null일 수 없습니다"),
    GENDER_REQUIRED("성별은 null일 수 없습니다"),
    NOT_ADMIN("운영진이 아닙니다")
    ;

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
