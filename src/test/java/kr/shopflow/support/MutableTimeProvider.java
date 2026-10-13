package kr.shopflow.support;

import java.time.Duration;
import java.time.Instant;
import kr.shopflow.common.TimeProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** 시험용 시각 공급자. 시각을 고정하거나 앞으로 돌릴 수 있다. 고정하지 않으면 시스템 시계를 쓴다. */
@Component
@Primary
@Profile("test")
public class MutableTimeProvider implements TimeProvider {

    private volatile Instant fixed;

    @Override
    public Instant now() {
        Instant value = fixed;
        return value != null ? value : Instant.now();
    }

    public void fixAt(Instant instant) {
        this.fixed = instant;
    }

    public void advance(Duration duration) {
        this.fixed = now().plus(duration);
    }

    public void reset() {
        this.fixed = null;
    }
}
