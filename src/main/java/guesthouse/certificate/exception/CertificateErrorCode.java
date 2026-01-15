package guesthouse.certificate.exception;

import guesthouse.common.exception.ErrorCode;

public enum CertificateErrorCode implements ErrorCode {

    FILE_TYPE_NOT_SUPPORTED("파일타입을 확인해주세요"),
    FILE_UPLOAD_FAILED("인증서 파일 저장에 실패하였습니다.", 500),
    ;

    private final String message;
    private final int statusCode;

    CertificateErrorCode(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    CertificateErrorCode(String message) {
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
