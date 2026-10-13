package kr.shopflow.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import kr.shopflow.common.error.UnprocessableException;
import kr.shopflow.common.idempotency.IdempotencyProperties;
import kr.shopflow.common.idempotency.IdempotencyService;
import kr.shopflow.common.idempotency.IdempotencyService.Begin;
import kr.shopflow.common.idempotency.IdempotencyService.Outcome;
import kr.shopflow.support.ConcurrencyTestSupport;
import kr.shopflow.support.MutableTimeProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** 멱등키: (범위, 키)가 유일하고, 같은 키에 내용이 다르면 거절한다(T015, ADR-0002, SC-003). */
class IdempotencyServiceTest extends IntegrationTestBase {

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
    void 처음_보는_키는_진행하고_처리_중에_다시_오면_처리_중을_알린다() {
        String key = newKey();
        assertThat(service.begin("CHECKOUT", key, "hash-1").outcome()).isEqualTo(Outcome.STARTED);
        assertThat(service.begin("CHECKOUT", key, "hash-1").outcome()).isEqualTo(Outcome.IN_PROGRESS);
    }

    @Test
    void 같은_키와_같은_내용은_저장된_결과를_그대로_돌려준다() {
        String key = newKey();
        service.begin("CHECKOUT", key, "hash-1");
        service.complete("CHECKOUT", key, "{\"orderId\":\"o-1\"}");
        for (int i = 0; i < 10; i++) {
            Begin again = service.begin("CHECKOUT", key, "hash-1");
            assertThat(again.outcome()).isEqualTo(Outcome.REPLAY);
            assertThat(again.result()).contains("\"orderId\"").contains("o-1");
        }
    }

    @Test
    void 실패한_결과도_저장해서_그대로_돌려준다() {
        String key = newKey();
        service.begin("CHECKOUT", key, "hash-1");
        service.fail("CHECKOUT", key, "{\"error\":\"OUT_OF_STOCK\"}");
        Begin again = service.begin("CHECKOUT", key, "hash-1");
        assertThat(again.outcome()).isEqualTo(Outcome.REPLAY);
        assertThat(again.result()).contains("OUT_OF_STOCK");
    }

    @Test
    void 같은_키에_다른_내용은_처리_중이든_끝났든_거절한다() {
        String key = newKey();
        service.begin("CHECKOUT", key, "hash-1");
        assertThatThrownBy(() -> service.begin("CHECKOUT", key, "hash-2")).isInstanceOf(UnprocessableException.class);
        service.complete("CHECKOUT", key, "{}");
        assertThatThrownBy(() -> service.begin("CHECKOUT", key, "hash-2")).isInstanceOf(UnprocessableException.class);
    }

    @Test
    void 범위가_다르면_같은_키도_따로_센다() {
        String key = newKey();
        assertThat(service.begin("CHECKOUT", key, "hash-1").outcome()).isEqualTo(Outcome.STARTED);
        assertThat(service.begin("REFUND", key, "hash-1").outcome()).isEqualTo(Outcome.STARTED);
    }

    @Test
    void 같은_키의_동시_요청_100건_중_하나만_처리한다() throws Exception {
        String key = newKey();
        List<Begin> results = ConcurrencyTestSupport.runConcurrently(100, () -> service.begin("CHECKOUT", key, "h"));
        assertThat(results).filteredOn(b -> b.outcome() == Outcome.STARTED).hasSize(1);
        assertThat(results).filteredOn(b -> b.outcome() == Outcome.IN_PROGRESS).hasSize(99);
    }

    @Test
    void 같은_키를_순서대로_10번_보내도_진행은_한_번이다() {
        String key = newKey();
        long started = 0;
        for (int i = 0; i < 10; i++) {
            if (service.begin("CHECKOUT", key, "h").outcome() == Outcome.STARTED) {
                started++;
                service.complete("CHECKOUT", key, "{}");
            }
        }
        assertThat(started).isEqualTo(1);
    }

    @Test
    void 끝난_키는_보관_기간이_지나면_정리_작업이_지운다() {
        String key = newKey();
        service.begin("CHECKOUT", key, "h");
        service.complete("CHECKOUT", key, "{}");
        assertThat(service.deleteExpired()).isZero();

        time.advance(properties.ttl().plus(Duration.ofMinutes(1)));
        assertThat(service.deleteExpired()).isGreaterThanOrEqualTo(1);
        assertThat(service.begin("CHECKOUT", key, "h").outcome()).isEqualTo(Outcome.STARTED);
    }
}
