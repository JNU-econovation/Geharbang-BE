package guesthouse.review.exception;

import guesthouse.common.exception.ErrorCode;

public enum ReviewErrorCode implements ErrorCode {

    NOT_FOUND("리뷰가 존재하지 않습니다.", 404),
    DUPLICATED("이미 작성한 리뷰가 있습니다.", 409),
    FORBIDDEN("리뷰를 수정하거나 삭제할 권한이 없습니다.", 403),
    STAFF_RECRUITMENT_REVIEW_FORBIDDEN("합격한 스텝 지원자만 리뷰를 작성할 수 있습니다.", 403),
    INVALID_RATING("별점은 1점 이상 5점 이하로 입력해주세요."),
    CONTENT_REQUIRED("리뷰 내용을 입력해주세요."),
    ;

    private final String message;
    private final int statusCode;

    ReviewErrorCode(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    ReviewErrorCode(String message) {
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
