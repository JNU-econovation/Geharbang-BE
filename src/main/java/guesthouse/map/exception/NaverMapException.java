package guesthouse.map.exception;

import guesthouse.common.exception.ErrorCode;
import guesthouse.common.exception.GuestHouseException;

public class NaverMapException extends GuestHouseException {

    public NaverMapException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NaverMapException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
