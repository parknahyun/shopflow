package kr.shopflow.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.shopflow.common.Money;
import org.junit.jupiter.api.Test;

/** 금액은 원 단위 정수만 쓰고 부동소수점을 쓰지 않는다(헌법 원칙 IX, T014). */
class MoneyTest {

    @Test
    void 원_단위_정수로_만든다() {
        assertThat(Money.ofWon(12_000).won()).isEqualTo(12_000L);
    }

    @Test
    void 더하기() {
        assertThat(Money.ofWon(1_500).plus(Money.ofWon(2_500))).isEqualTo(Money.ofWon(4_000));
    }

    @Test
    void 수량을_곱한다() {
        assertThat(Money.ofWon(3_300).times(3)).isEqualTo(Money.ofWon(9_900));
    }

    @Test
    void 영원은_허용한다() {
        assertThat(Money.ofWon(0)).isEqualTo(Money.ZERO);
    }

    @Test
    void 음수는_거절한다() {
        assertThatThrownBy(() -> Money.ofWon(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 음수_수량은_거절한다() {
        assertThatThrownBy(() -> Money.ofWon(1_000).times(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 더하기_오버플로는_거절한다() {
        assertThatThrownBy(() -> Money.ofWon(Long.MAX_VALUE).plus(Money.ofWon(1)))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void 곱하기_오버플로는_거절한다() {
        assertThatThrownBy(() -> Money.ofWon(Long.MAX_VALUE).times(2)).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void 금액_클래스는_부동소수점_필드를_갖지_않는다() {
        for (var field : Money.class.getDeclaredFields()) {
            assertThat(field.getType()).isNotIn(double.class, float.class, Double.class, Float.class);
        }
    }
}
