package guesthouse.application.exception;

import guesthouse.common.exception.ErrorCode;
import guesthouse.common.exception.GuestHouseException;

public class ApplicationException extends GuestHouseException {
    public ApplicationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
