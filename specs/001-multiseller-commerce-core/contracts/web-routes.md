# 계약: 화면 경로

서버 렌더링 웹 애플리케이션이므로 계약은 화면 경로와 폼 요청이다. 모든 상태 변경 요청(`POST`)은 사이트 간 요청 위조 방어 토큰과 권한 검사를 거친다. 결제·취소·승인 요청은 멱등키를 폼에 숨은 값으로 담는다.

## 공개 (로그인 불필요)

| 메서드 | 경로 | 설명 | 명세 |
|--------|------|------|------|
| GET | `/` , `/products` | 판매 중 상품 목록. 쿼리 `q`(이름 검색), `seller`(판매자별 모아보기), `page` | FR-005 |
| GET | `/products/{공개식별자}` | 상품 상세와 옵션 선택. 품절 조합은 선택 불가로 표시 | FR-005 |
| GET | `/sellers/{공개식별자}` | 판매자별 상품 모아보기 | FR-005 |
| GET, POST | `/signup` | 고객 가입 | FR-001 |
| GET, POST | `/login` | 로그인(고객, 판매자, 운영자 공용) | FR-001 |

## 고객 (고객 계정만)

| 메서드 | 경로 | 설명 | 명세 |
|--------|------|------|------|
| GET | `/cart` | 장바구니 보기 | FR-006 |
| POST | `/cart/items` | 판매 단위와 수량으로 담기 | FR-006 |
| POST | `/cart/items/{항목}/update` , `/remove` | 수량 변경, 빼기 | FR-006 |
| GET | `/checkout` | 주문 확인(금액, 배송지, 결제 방식 선택) | FR-007 |
| POST | `/checkout` | 결제 시작. 재고 선점과 결제 준비. 멱등키 필수 | FR-007, FR-009 |
| GET | `/payments/{공개식별자}/success` | 대행사에서 돌아온 결과. 서버가 대행사에 조회해 검증 후 확정 | FR-010 |
| GET | `/payments/{공개식별자}/fail` | 결제 실패 안내. 재고 반환 | FR-007 |
| GET | `/orders` , `/orders/{공개식별자}` | 내 주문과 하위 주문별 상태 | FR-014 |
| POST | `/orders/{주문}/sub-orders/{하위주문}/cancel` | 발송 전 취소. 멱등키 필수 | FR-013a, FR-013b |
| POST | `/orders/{주문}/sub-orders/{하위주문}/confirm` | 구매 확정 | FR-012 |

## 판매자 (판매자 계정만, 승인된 판매자)

| 메서드 | 경로 | 설명 | 명세 |
|--------|------|------|------|
| GET, POST | `/seller/signup` | 판매자 가입과 입점 신청 | FR-002 |
| GET | `/seller/products` | 내 상품 목록 | FR-004 |
| GET, POST | `/seller/products/new` | 상품과 옵션 조합 등록 | FR-003, FR-003a |
| GET, POST | `/seller/products/{상품}/edit` | 상품·옵션·재고 수정 | FR-003a |
| POST | `/seller/products/{상품}/suspend` | 판매 중지 | FR-003 |
| GET | `/seller/orders` , `/seller/orders/{하위주문}` | 내 하위 주문만 조회 | FR-004 |
| POST | `/seller/orders/{하위주문}/ship` | 송장 입력, 배송 중으로 변경 | FR-012 |
| POST | `/seller/orders/{하위주문}/deliver` | 배송 완료로 변경 | FR-012 |

## 운영자 (운영자 계정만)

| 메서드 | 경로 | 설명 | 명세 |
|--------|------|------|------|
| GET | `/admin/sellers?status=신청` | 입점 신청 목록 | FR-002 |
| POST | `/admin/sellers/{판매자}/approve` , `/reject` | 승인 또는 거절 | FR-002 |
| GET | `/admin/orders?status=확인필요` | 확인 필요 주문 목록(기록 확인용) | 엣지 케이스 |

## 오류 응답 규칙

- 권한 없음(다른 사람의 주문, 다른 판매자의 하위 주문): 존재 여부를 알리지 않도록 404로 응답한다.
- 허용되지 않는 상태 변경: 409와 한글 안내 문구.
- 재고 부족, 금액 불일치: 사용자에게 보이는 한글 안내와 함께 이전 화면으로 돌려보낸다.
- 같은 멱등키에 다른 내용: 422.
