package guesthouse.oauth2.exception;

import guesthouse.common.exception.ErrorCode;

public enum AuthErrorCode implements ErrorCode {

    LOGIN_REQUIRED("로그인이 필요합니다."),
    INVALID_AUTH_HEADER("인증 헤더 형식이 잘못되었습니다. Bearer로 시작해주세요."),
    TOKEN_EXPIRED("토큰이 만료되었습니다."),
    INVALID_TOKEN_SUBJECT("토큰의 용도가 올바르지 않습니다"),
    INVALID_TOKEN("유효하지 않는 토큰입니다."),
    ;

    private final String message;
    private final int statusCode;

    AuthErrorCode(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    AuthErrorCode(String message) {
        this(message, 401);
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
