package kr.shopflow.common.error;

import org.springframework.http.HttpStatus;

/** 없거나 권한이 없는 대상. 존재 여부를 알리지 않기 위해 둘 다 404로 응답한다. */
public class NotFoundException extends BusinessException {

    public NotFoundException() {
        super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.defaultMessage());
    }
}
