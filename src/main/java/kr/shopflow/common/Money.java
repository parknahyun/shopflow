package kr.shopflow.common;

/**
 * 원 단위 정수 금액 값 객체(헌법 원칙 IX). 부동소수점을 쓰지 않고, 음수와 오버플로를 거절한다.
 * 환불 차감처럼 음수가 필요한 계산은 호출하는 쪽에서 큰 값에서 작은 값을 빼서 구한다.
 */
public record Money(long won) {

    public static final Money ZERO = new Money(0);

    public Money {
        if (won < 0) {
            throw new IllegalArgumentException("금액은 0원 이상이어야 합니다.");
        }
    }

    public static Money ofWon(long won) {
        return new Money(won);
    }

    /** 합계가 long 범위를 넘으면 {@link ArithmeticException}을 던진다. */
    public Money plus(Money other) {
        return new Money(Math.addExact(won, other.won));
    }

    /** 수량을 곱한다. 수량이 음수이면 거절하고, 결과가 long 범위를 넘으면 {@link ArithmeticException}을 던진다. */
    public Money times(long quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("수량은 0 이상이어야 합니다.");
        }
        return new Money(Math.multiplyExact(won, quantity));
    }
}
