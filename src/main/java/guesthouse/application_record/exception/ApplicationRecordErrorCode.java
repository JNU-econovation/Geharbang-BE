package guesthouse.application_record.exception;

import guesthouse.common.exception.ErrorCode;

public enum ApplicationRecordErrorCode implements ErrorCode {

    NOT_ALLOWED("권한이 없습니다."),
    NOT_FOUND("해당 지원 기록이 없습니다."),
    DUPLICATED_APPLICATION("이미 지원한 스탭 공고입니다."),
    DESERIALIZATION_FAILED("역직렬화에 실패했습니다.", 500),

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
