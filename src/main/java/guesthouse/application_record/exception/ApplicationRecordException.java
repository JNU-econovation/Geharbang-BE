package guesthouse.application_record.exception;

import guesthouse.common.exception.ErrorCode;
import guesthouse.common.exception.GuestHouseException;

public class ApplicationRecordException extends GuestHouseException {

    public ApplicationRecordException(ErrorCode errorCode) {
        super(errorCode);
    }
}
