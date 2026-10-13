package kr.shopflow.common;

import java.time.Instant;
import org.springframework.stereotype.Component;

/** 운영용 시각 공급자(시스템 시계). 시험에서는 고정 시각 구현으로 바꾼다. */
@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
