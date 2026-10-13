package kr.shopflow.common.error;

/** 오류 코드. 프론트엔드는 이 코드로 화면 문구를 고른다(T156). 메시지는 한글이다. */
public enum ErrorCode {
    NOT_FOUND("찾을 수 없습니다."),
    NOT_ALLOWED_TRANSITION("허용되지 않는 상태 변경입니다."),
    IDEMPOTENCY_KEY_REUSED("같은 요청 식별값에 다른 내용이 들어왔습니다."),
    IDEMPOTENCY_IN_PROGRESS("같은 요청이 아직 처리 중입니다."),
    VALIDATION_FAILED("입력값을 확인해 주세요."),
    METHOD_NOT_ALLOWED("허용되지 않는 요청 방법입니다."),
    UNAUTHENTICATED("로그인이 필요합니다."),
    FORBIDDEN("이 기능을 쓸 수 없는 계정입니다."),
    INTERNAL_ERROR("잠시 후 다시 시도해 주세요.");

    private final String defaultMessage;

    ErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
