package guesthouse.map.exception;

import guesthouse.common.exception.ErrorCode;

public enum NaverMapErrorCode implements ErrorCode {

    NAVER_MAP_CONFIG_MISSING("네이버 지도 API 설정이 누락되었습니다.", 500),
    NAVER_LOCAL_CONFIG_MISSING("네이버 지역 검색 API 설정이 누락되었습니다.", 500),
    NAVER_MAP_API_FAILED("네이버 지도 API 호출에 실패했습니다.", 502),
    NAVER_LOCAL_API_FAILED("네이버 지역 검색 API 호출에 실패했습니다.", 502),
    NAVER_MAP_RESPONSE_INVALID("네이버 지도 API 응답을 해석할 수 없습니다.", 502),
    ;

    private final String message;
    private final int statusCode;

    NaverMapErrorCode(String message, int statusCode) {
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
