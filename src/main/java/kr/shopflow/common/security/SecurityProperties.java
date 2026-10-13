package kr.shopflow.common.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 보안 설정값(T026, T168). 별도 프론트엔드가 뜨면 그 출처를 허용 목록에 넣는다.
 * 기본은 허용 출처가 없다(다른 출처의 브라우저 요청을 모두 거절한다).
 */
@ConfigurationProperties(prefix = "shopflow.security")
public record SecurityProperties(List<String> allowedOrigins) {

    public SecurityProperties {
        allowedOrigins = allowedOrigins != null ? List.copyOf(allowedOrigins) : List.of();
    }
}
