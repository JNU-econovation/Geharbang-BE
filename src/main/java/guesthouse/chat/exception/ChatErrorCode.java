package guesthouse.chat.exception;

import guesthouse.common.exception.ErrorCode;

public enum ChatErrorCode implements ErrorCode {

    ROOM_NOT_FOUND("채팅방이 존재하지 않습니다.", 404),
    NOT_PARTICIPANT("채팅방 참여자가 아닙니다.", 403),
    MESSAGE_REQUIRED("메시지 내용을 입력해주세요.", 400),
    INVALID_ROOM_TARGET("채팅방 생성 대상이 올바르지 않습니다.", 400),
    SELF_CHAT_NOT_ALLOWED("본인과는 채팅할 수 없습니다.", 400),
    ;

    private final String message;
    private final int statusCode;

    ChatErrorCode(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return statusCode;
    }

    @Override
    public String getErrorCode() {
        return name();
    }
}
