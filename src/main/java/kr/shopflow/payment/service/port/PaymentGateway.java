package kr.shopflow.payment.service.port;

import kr.shopflow.common.Money;

/**
 * 결제 대행사 포트(T166, contracts/payment-gateway-port.md, ADR-0003).
 *
 * <p>도메인 코드는 대행사를 직접 부르지 않고 이 포트만 쓴다. 운영 구현은 3단계 이후(US3)에 만들고,
 * 그 전에는 호출하면 예외를 던진다. 어떤 입력도 카드번호 원문을 담지 않는다.
 */
public interface PaymentGateway {

    /** 결제를 준비한다(결제창 접속 정보 또는 가상계좌 정보). */
    PrepareResult prepare(String orderPublicId, Money amount, String method, String idempotencyKey);

    /** 즉시 결제를 승인한다. 금액은 서버가 저장한 주문 총액이어야 한다. */
    ConfirmResult confirm(String gatewayPaymentKey, String orderPublicId, Money amount, String idempotencyKey);

    /** 결과를 서버가 직접 확인한다. 만료 처리 전에는 항상 먼저 부른다. */
    QueryResult query(String gatewayPaymentKey);

    /** 환불을 요청한다. 가상계좌 결제는 환불 계좌가 필수다. */
    RefundResult refund(String gatewayPaymentKey, Money amount, String reason, RefundAccount account,
            String idempotencyKey);

    /** 결제창 접속 정보 또는 가상계좌 정보. */
    record PrepareResult(String gatewayPaymentKey, String checkoutUrl, String bank, String accountNumber,
            java.time.Instant depositDueAt) {}

    /** 승인 결과. */
    record ConfirmResult(Status status, Money amount) {
        public enum Status { SUCCESS, FAILED }
    }

    /** 현재 결제 상태. */
    record QueryResult(Status status, Money amount) {
        public enum Status { AWAITING_CONFIRM, AWAITING_DEPOSIT, SUCCESS, FAILED, EXPIRED }
    }

    /** 환불 결과. 결과를 모르면 UNKNOWN이며 조회로 확정한다. */
    record RefundResult(Status status, String gatewayRefundKey) {
        public enum Status { COMPLETED, FAILED, UNKNOWN }
    }

    /** 가상계좌 환불 계좌. 로그에 남기지 않는다. */
    record RefundAccount(String bank, String accountNumber, String holder) {
        @Override
        public String toString() {
            return "RefundAccount[" + bank + ", ****]";
        }
    }
}
