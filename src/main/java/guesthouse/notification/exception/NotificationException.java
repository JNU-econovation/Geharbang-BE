package guesthouse.notification.exception;

import guesthouse.common.exception.ErrorCode;
import guesthouse.common.exception.GuestHouseException;

public class NotificationException extends GuestHouseException {

    public NotificationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
