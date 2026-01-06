package guesthouse.common.exception;

public class GuestHouseException extends RuntimeException{

    private final ErrorCode errorCode;

    public GuestHouseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
