package kr.shopflow.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import kr.shopflow.common.idempotency.IdempotencyProperties;
import kr.shopflow.common.idempotency.IdempotencyService;
import kr.shopflow.common.idempotency.IdempotencyService.Begin;
import kr.shopflow.common.idempotency.IdempotencyService.Outcome;
import kr.shopflow.support.ConcurrencyTestSupport;
import kr.shopflow.support.MutableTimeProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 처리 중인 키의 점유 만료(T171, FR-009, ADR-0009 B6).
 * 서버가 처리 도중 죽어도 키가 "처리 중"에 영구히 고착되지 않는다.
 */
class IdempotencyLeaseTest extends IntegrationTestBase {

    @Autowired
    IdempotencyService service;

    @Autowired
    IdempotencyProperties properties;

    @Autowired
    MutableTimeProvider time;

    @AfterEach
    void resetTime() {
        time.reset();
    }

    private static String newKey() {
        return UUID.randomUUID().toString();
    }

    @Test
    void 점유_시간_안에는_다른_요청이_이어받지_못한다() {
        String key = newKey();
        service.begin("PAYMENT_CONFIRM", key, "h");
        time.advance(properties.claimDuration().minusSeconds(1));
        assertThat(service.begin("PAYMENT_CONFIRM", key, "h").outcome()).isEqualTo(Outcome.IN_PROGRESS);
    }

    @Test
    void 점유_시간이_지나면_다른_요청이_이어받는다() {
        String key = newKey();
        service.begin("PAYMENT_CONFIRM", key, "h");
        time.advance(properties.claimDuration().plusSeconds(1));
        assertThat(service.begin("PAYMENT_CONFIRM", key, "h").outcome()).isEqualTo(Outcome.CLAIM_EXPIRED);
        // 이어받은 직후에는 점유가 갱신되어 또 다른 요청은 기다린다.
        assertThat(service.begin("PAYMENT_CONFIRM", key, "h").outcome()).isEqualTo(Outcome.IN_PROGRESS);
    }

    @Test
    void 이어받은_요청이_마무리하면_저장된_결과가_돌아간다() {
        String key = newKey();
        service.begin("PAYMENT_CONFIRM", key, "h");
        time.advance(properties.claimDuration().plusSeconds(1));
        service.begin("PAYMENT_CONFIRM", key, "h");
        service.complete("PAYMENT_CONFIRM", key, "{\"paid\":true}");
        Begin replay = service.begin("PAYMENT_CONFIRM", key, "h");
        assertThat(replay.outcome()).isEqualTo(Outcome.REPLAY);
        assertThat(replay.result()).contains("paid");
    }

    @Test
    void 이어받은_요청이_실패로_바꾸면_실패_결과가_돌아가고_재시도는_새_키를_쓴다() {
        String key = newKey();
        service.begin("PAYMENT_CONFIRM", key, "h");
        time.advance(properties.claimDuration().plusSeconds(1));
        service.begin("PAYMENT_CONFIRM", key, "h");
        service.fail("PAYMENT_CONFIRM", key, "{\"error\":\"GATEWAY_TIMEOUT\"}");
        assertThat(service.begin("PAYMENT_CONFIRM", key, "h").result()).contains("GATEWAY_TIMEOUT");
        assertThat(service.begin("PAYMENT_CONFIRM", newKey(), "h").outcome()).isEqualTo(Outcome.STARTED);
    }

    @Test
    void 점유_시간이_지난_키를_동시에_100건이_이어받으려_해도_하나만_이어받는다() throws Exception {
        String key = newKey();
        service.begin("PAYMENT_CONFIRM", key, "h");
        time.advance(properties.claimDuration().plus(Duration.ofSeconds(1)));
        List<Begin> results = ConcurrencyTestSupport.runConcurrently(100,
                () -> service.begin("PAYMENT_CONFIRM", key, "h"));
        assertThat(results).filteredOn(b -> b.outcome() == Outcome.CLAIM_EXPIRED).hasSize(1);
        assertThat(results).filteredOn(b -> b.outcome() == Outcome.IN_PROGRESS).hasSize(99);
    }
}
