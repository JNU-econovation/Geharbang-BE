package guesthouse.guestHousePost.exception;

import guesthouse.common.exception.ErrorCode;
import guesthouse.common.exception.GuestHouseException;

public class GuestHousePostException extends GuestHouseException {

    public GuestHousePostException(ErrorCode errorCode) {
        super(errorCode);
    }
}