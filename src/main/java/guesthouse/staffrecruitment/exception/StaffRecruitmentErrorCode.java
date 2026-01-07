package guesthouse.staffrecruitment.exception;

import guesthouse.common.exception.ErrorCode;

public enum StaffRecruitmentErrorCode implements ErrorCode {

    NOT_FOUND("스태프 모집글이 존재하지 않습니다.", 404),
    NOT_FOUND_QUESTION("질문이 존재하지 않습니다.", 404),
    ;

    private final String message;
    private final int statusCode;

    StaffRecruitmentErrorCode(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    StaffRecruitmentErrorCode(String message) {
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

