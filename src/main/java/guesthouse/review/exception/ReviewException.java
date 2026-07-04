package guesthouse.review.exception;

import guesthouse.common.exception.ErrorCode;
import guesthouse.common.exception.GuestHouseException;

public class ReviewException extends GuestHouseException {

    public ReviewException(ErrorCode errorCode) {
        super(errorCode);
    }
}
