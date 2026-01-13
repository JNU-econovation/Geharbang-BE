package guesthouse.certificate.exception;

import guesthouse.common.exception.ErrorCode;
import guesthouse.common.exception.GuestHouseException;

public class CertificateException extends GuestHouseException {

    public CertificateException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CertificateException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
