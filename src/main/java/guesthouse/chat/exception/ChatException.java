package guesthouse.chat.exception;

import guesthouse.common.exception.ErrorCode;
import guesthouse.common.exception.GuestHouseException;

public class ChatException extends GuestHouseException {

    public ChatException(ErrorCode errorCode) {
        super(errorCode);
    }
}
