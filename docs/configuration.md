# 설정값 문서 (T168)

값이 정해지지 않은 항목은 코드에 박지 않고 설정값으로 둔다. 아래 기본값은 **초기값**이며 운영 정책이 정해지면 `application.yml`(또는 환경 변수)로 바꾼다. 표의 "상태"가 `확정`인 값만 명세나 ADR에서 정해진 값이다.

| 항목 | 설정 키 | 기본값 | 상태 | 근거 |
|------|---------|--------|------|------|
| 재고 확보 유지 시간(즉시 결제) | `shopflow.inventory.reservation-ttl.immediate-payment` | 15m | 확정 | FR-008 |
| 재고 확보 유지 시간(입금 대기) | `shopflow.inventory.reservation-ttl.deposit-wait` | 24h | 확정 | FR-008, ADR-0009 |
| 자동 구매 확정 | `shopflow.delivery.auto-confirm-after` | 7d | 확정 | FR-012 |
| 예약 작업 주기 | `shopflow.scheduler.interval` | 1m | 확정 | UC-17~19 |
| 멱등키 만료 시간 | `shopflow.idempotency.ttl` | 24h | 초기값 | ADR-0002 |
| 멱등키 점유 시간 | `shopflow.idempotency.claim-duration` | 2m | 초기값 | ADR-0009 B6 |
| 허용 출처(CORS) | `shopflow.security.allowed-origins` | 없음(모두 거절) | 초기값 | T026 |
| 입점 서류 허용 형식 | `shopflow.storage.document-types` | pdf, jpeg, png | 초기값 | FR-002d |
| 입점 서류 크기 상한 | `shopflow.storage.max-document-bytes` | 10MB | 초기값 | FR-002d |
| 상품 사진 허용 형식 | `shopflow.storage.photo-types` | jpeg, png, webp | 초기값 | FR-003 |
| 상품 사진 크기 상한 | `shopflow.storage.max-photo-bytes` | 5MB | 초기값 | FR-003 |
| 파일 저장 위치 | `shopflow.storage.root` | `storage` | 초기값 | T025 |
| 멱등키를 받는 위치 | HTTP 헤더 `Idempotency-Key` | — | 초기값 | ADR-0002 |

## 아직 값을 정하지 않은 항목

아래는 해당 기능을 만드는 단계에서 설정값으로 추가한다. 값은 이 문서에서 지어내지 않는다.

- 목록 페이지 크기와 정렬 (US2)
- 결제 대행사 호출 제한 시간, 재시도 횟수와 주기, 재조회 주기 (US3)
- 입금 기한 (US3)
- 고객당 결제 대기 주문 수 상한: ADR-0009 기본 3건
- 한 주문의 판매 단위별 수량 상한: 기본 10개
- 입금 대기 유지 시간(기본 24시간)과 판매 단위별 입금 대기 상한
- 환불 재시도 주기
- 환불 계좌 암호화 키 출처: 환경 변수나 비밀 저장소에서 읽고 **저장소에 커밋하지 않는다**
