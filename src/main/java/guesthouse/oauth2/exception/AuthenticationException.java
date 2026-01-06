package guesthouse.oauth2.exception;

import guesthouse.common.exception.ErrorCode;
import guesthouse.common.exception.GuestHouseException;

public class AuthenticationException extends GuestHouseException {

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
