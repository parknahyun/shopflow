package kr.shopflow.common.idempotency;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 멱등키 설정(T168, ADR-0002).
 *
 * @param ttl           완료·실패한 키를 보관하는 기간. 지나면 정리 작업이 지운다.
 * @param claimDuration 처리 중인 키의 점유 시간. 지나면 다른 요청이 이어받는다.
 */
@ConfigurationProperties(prefix = "shopflow.idempotency")
public record IdempotencyProperties(Duration ttl, Duration claimDuration) {

    public IdempotencyProperties {
        ttl = ttl != null ? ttl : Duration.ofHours(24);
        claimDuration = claimDuration != null ? claimDuration : Duration.ofMinutes(2);
    }
}
