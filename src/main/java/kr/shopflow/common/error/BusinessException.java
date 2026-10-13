package kr.shopflow.common.error;

import org.springframework.http.HttpStatus;

/** 업무 규칙 위반. 상태 코드와 오류 코드를 함께 가진다. */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode code;

    protected BusinessException(HttpStatus status, ErrorCode code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() {
        return status;
    }

    public ErrorCode code() {
        return code;
    }
}
