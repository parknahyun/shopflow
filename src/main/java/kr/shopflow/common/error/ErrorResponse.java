package kr.shopflow.common.error;

/** 오류 응답 본문. 프론트엔드는 {@code code}로 화면 문구를 고르고, {@code message}는 한글 기본 안내다. */
public record ErrorResponse(String code, String message) {

    public static ErrorResponse of(ErrorCode code) {
        return new ErrorResponse(code.name(), code.defaultMessage());
    }

    public static ErrorResponse of(ErrorCode code, String message) {
        return new ErrorResponse(code.name(), message);
    }
}
