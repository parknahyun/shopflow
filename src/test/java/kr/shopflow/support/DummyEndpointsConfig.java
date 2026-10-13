package kr.shopflow.support;

import java.util.Map;
import kr.shopflow.common.error.ConflictException;
import kr.shopflow.common.error.NotFoundException;
import kr.shopflow.common.idempotency.IdempotencyService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * 시험용 더미 엔드포인트(T167). 실제 엔드포인트가 생기기 전에도 보안, 오류 응답, 멱등 규칙을
 * 공허하게 통과하지 않고 시험할 수 있게 한다. 시험 프로필에서만 켜진다.
 */
@RestController
@Profile("test")
public class DummyEndpointsConfig {

    private final IdempotencyService idempotencyService;

    public DummyEndpointsConfig(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    @GetMapping("/dummy/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Map<String, String> customerOnly() {
        return Map.of("area", "customer");
    }

    @GetMapping("/dummy/seller")
    @PreAuthorize("hasRole('SELLER')")
    public Map<String, String> sellerOnly() {
        return Map.of("area", "seller");
    }

    @GetMapping("/dummy/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminOnly() {
        return Map.of("area", "admin");
    }

    @PostMapping("/dummy/customer/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> customerWrite() {
        return ResponseEntity.ok(Map.of("created", "true"));
    }

    /** 다른 고객의 주문처럼 존재 여부를 알리면 안 되는 대상. */
    @GetMapping("/dummy/orders/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Map<String, String> otherCustomersOrder(@PathVariable String id) {
        throw new NotFoundException();
    }

    @PostMapping("/dummy/sub-orders/transition")
    @PreAuthorize("hasRole('SELLER')")
    public Map<String, String> notAllowedTransition() {
        throw ConflictException.notAllowedTransition("배송 완료 뒤에는 발송 전으로 되돌릴 수 없습니다.");
    }

    @PostMapping("/dummy/idempotent")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Map<String, String> idempotent(@RequestHeader("Idempotency-Key") String key, @RequestBody String body) {
        var begin = idempotencyService.begin("DUMMY", key, Integer.toHexString(body.hashCode()));
        return Map.of("outcome", begin.outcome().name());
    }
}
