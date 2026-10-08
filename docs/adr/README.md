# 아키텍처 결정 기록 (ADR)

중요한 기술·업무 결정을 "왜 그렇게 정했는지"와 함께 남기는 문서 모음이다. 결정이 바뀌면 기존 문서를 고치지 않고 새 ADR을 쓰고, 이전 ADR의 상태를 "대체됨"으로 바꾼다.

세부 조사 내용은 [research.md](../../specs/001-multiseller-commerce-core/research.md)에 있다. ADR에는 결정과 근거의 핵심만 쓰고 세부는 링크로 가리킨다.

## 상태 값

| 상태 | 뜻 |
|------|----|
| 제안됨 | 초안이다. 아직 사람이 확정하지 않았다. |
| 승인됨 | 확정되어 따르고 있다. |
| 대체됨 | 새 ADR로 바뀌었다. 대체한 ADR 번호를 적는다. |

## 목록

| 번호 | 제목 | 상태 | 관련 이슈 |
|------|------|------|-----------|
| [0001](0001-inventory-reservation.md) | 재고 선점 방식 | 제안됨 | #3 |
| [0002](0002-idempotency-key.md) | 멱등키 설계 | 제안됨 | #3 |
| [0003](0003-payment-verification-and-gateway.md) | 결제 검증과 대행사 연동 | 제안됨 | #3 |
| [0004](0004-seller-sub-orders.md) | 판매자별 하위 주문 구조 | 승인됨 | #3 |
| [0005](0005-settlement-basis-purchase-confirmation.md) | 정산 기준 시점: 구매 확정 | 승인됨 | #3 |
| [0006](0006-technology-stack-and-structure.md) | 기본 기술 스택과 구조 | 승인됨 | #3 |
| [0007](0007-context-deployment-strategy.md) | 컨텍스트별 배포 목표와 단계적 전환 | 승인됨 | #3, #4 |

## 새 ADR 작성 방법

1. 이슈를 만들고 브랜치에서 작업한다(헌법 X번 원칙).
2. [template.md](template.md)를 복사해 다음 번호로 파일을 만든다. 파일 이름은 `번호-영문-요약.md` 형식이다.
3. 이 목록에 한 줄을 추가한다.
