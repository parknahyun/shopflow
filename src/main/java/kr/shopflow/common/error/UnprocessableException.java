package kr.shopflow.common.error;

import org.springframework.http.HttpStatus;

/** 같은 멱등키에 다른 내용이 들어온 경우 등 처리할 수 없는 요청(422). */
public class UnprocessableException extends BusinessException {

    public UnprocessableException(ErrorCode code, String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }
}
