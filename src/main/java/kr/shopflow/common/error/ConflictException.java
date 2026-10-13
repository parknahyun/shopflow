package kr.shopflow.common.error;

import org.springframework.http.HttpStatus;

/** 허용되지 않는 상태 변경이나 먼저 처리된 요청과의 충돌(409). */
public class ConflictException extends BusinessException {

    public ConflictException(ErrorCode code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }

    public static ConflictException notAllowedTransition(String detail) {
        return new ConflictException(ErrorCode.NOT_ALLOWED_TRANSITION,
                ErrorCode.NOT_ALLOWED_TRANSITION.defaultMessage() + " " + detail);
    }
}
