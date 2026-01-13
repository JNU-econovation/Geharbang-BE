package guesthouse.staffrecruitment.exception;

import guesthouse.common.exception.ErrorCode;
import guesthouse.common.exception.GuestHouseException;

public class StaffRecruitmentException extends GuestHouseException {

    public StaffRecruitmentException(ErrorCode errorCode) {
        super(errorCode);
    }
}
