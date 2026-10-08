---

description: "멀티 판매자 이커머스 핵심 구매·판매 흐름 작업 목록"
---

# 작업 목록: 멀티 판매자 이커머스 핵심 구매·판매 흐름

**입력**: `/specs/001-multiseller-commerce-core/`의 설계 문서

**전제 문서**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md), [data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md), [usecase-details.md](usecase-details.md), ADR-0001~0008(`docs/adr/`)

**관련 이슈**: #3

**테스트**: 헌법 원칙 I(테스트 우선)에 따라 **모든 사용자 스토리에 테스트 작업이 있고, 구현보다 먼저 작성해서 실패를 확인한다.** 테스트 작업을 건너뛰고 구현 작업을 시작해서는 안 된다.

**조직 방식**: 작업은 사용자 스토리별로 묶어 각 스토리를 독립적으로 구현하고 시험할 수 있게 한다. 각 스토리는 헌법 원칙 X에 따라 이슈를 연결한 브랜치에서 작업하고 PR로 완료를 요청한다.

## 형식: `- [ ] T### [P] [US#] 설명과 파일 경로`

- **[P]**: 병렬로 할 수 있다(서로 다른 파일이고 끝나지 않은 작업에 의존하지 않는다).
- **[US#]**: 사용자 스토리 번호(US1~US5). 설정, 기반, 마무리 단계에는 붙이지 않는다.
  - **US1**: 판매자 입점과 상품 등록(P1)
  - **US2**: 고객 가입과 상품 탐색(P1)
  - **US3**: 장바구니, 결제, 주문(P1)
  - **US4**: 판매자 배송 처리와 구매 확정(P2)
  - **US5**: 고객 발송 전 취소(P2)
- 설명에는 정확한 파일 경로를 적는다. 데이터 모델의 제약은 원문 그대로 인용한다.

## 경로 규칙

소스 루트는 `src/main/java/kr/shopflow/`(아래 `…/`로 줄여 쓴다), 테스트 루트는 `src/test/java/kr/shopflow/`(아래 `테스트/`)다. 컨텍스트 패키지(`account`, `product`, `order`, `inventory`, `payment`, `delivery`, `common`) 안은 `api`(스프링 데이터 REST 리소스와 동작 컨트롤러), `service`(업무 규칙), `repository`(스프링 데이터 JPA)로 나눈다. 화면은 `web`, API 클라이언트는 `client` 패키지다(plan.md의 계층 구조). 클래스와 파일 이름은 영문 코드 식별자를 쓴다.

---

## 1단계: 설정 (공통 기반 마련)

**목적**: 프로젝트 초기화와 개발 규율 준비

- [ ] T001 `build.gradle`, `settings.gradle` 작성: 자바 21, 스프링 부트 3.x, 의존성(스프링 웹, 타임리프, 스프링 시큐리티, 스프링 데이터 JPA, 스프링 데이터 REST, 밸리데이션, 플라이웨이, 포스트그레스큐엘 드라이버, 제이유닛 5, AssertJ, 스프링 시큐리티 테스트, 테스트컨테이너스 포스트그레스큐엘, ArchUnit)
- [ ] T002 [P] `src/main/resources/application.yml` 작성: 설정값(재고 확보 유지 시간 즉시 결제 15분과 입금 대기 24시간, 자동 구매 확정 7일, 예약 작업 주기 1분), `spring.data.rest.base-path=/api`, `spring.data.rest.detection-strategy=annotated`(어노테이션을 붙인 리포지토리만 노출)
- [ ] T003 [P] `src/test/resources/application-test.yml` 작성: 테스트용 설정과 결제 가짜 구현 사용 설정
- [ ] T004 [P] 패키지 골격 만들기: `…/web`, `…/client`, `…/account`, `…/product`, `…/order`, `…/inventory`, `…/payment`, `…/delivery`, `…/common` 각각에 `package-info.java`(컨텍스트 패키지는 `api`, `service`, `repository` 하위 패키지 포함)
- [ ] T005 [P] `.gitignore`, `.editorconfig` 작성(개인 설정과 빌드 산출물 제외)
- [ ] T006 [P] `.github/workflows/ci.yml` 작성: 푸시와 PR마다 전체 테스트 실행(테스트컨테이너스를 쓸 수 있는 환경). 실패한 테스트가 있으면 머지할 수 없다(헌법 원칙 I, X)
- [ ] T007 [P] `.github/pull_request_template.md`, `.github/ISSUE_TEMPLATE/feature.md` 작성: `Closes #번호`, 테스트 우선 확인, 보안 점검(결제, 인증, 개인정보), 한글 확인 항목(헌법 원칙 V, VI, X)
- [ ] T008 [P] `테스트/integration/IntegrationTestBase.java` 작성: 테스트컨테이너스로 포스트그레스큐엘을 띄우고 플라이웨이 마이그레이션을 적용하는 공통 기반 클래스
- [ ] T009 `docs/adr/`의 "제안됨" ADR 4건(0001 재고 선점, 0002 멱등키, 0003 결제 검증과 대행사, 0008 계층 구조)을 검토해 확정하고 상태를 갱신한다. 확정하지 않으면 이 ADR에 의존하는 작업(T020~T027, US3)을 시작하지 않는다
- [ ] T010 저장소에 브랜치 보호 규칙(`main` 직접 푸시 금지, 승인 1명 이상, CI 통과)을 설정한다(헌법 원칙 X). 설정 방법을 `docs/`에 기록한다

---

## 2단계: 기반 작업 (모든 스토리가 기다리는 선행 작업)

**목적**: 어떤 사용자 스토리보다 먼저 끝나야 하는 공통 기반

**⚠️ 중요**: 이 단계가 끝나기 전에는 사용자 스토리 작업을 시작할 수 없다.

### 기반 테스트 (먼저 작성하고 실패를 확인한다) ⚠️

- [ ] T011 [P] `테스트/unit/ArchitectureTest.java` 작성(ArchUnit): ① 다른 컨텍스트의 `repository` 패키지를 직접 쓰지 않는다 ② `web` 패키지는 `client`만 쓰고 `service`와 `repository`를 직접 참조하지 않는다 ③ 컨텍스트 사이에 JPA 연관(`@ManyToOne`, `@OneToMany` 등)을 두지 않고 식별자 값만 참조한다 ④ `inventory`와 `delivery`에는 `api` 패키지가 없다(ADR-0007, ADR-0008)
- [ ] T012 [P] `테스트/integration/RestExposureTest.java` 작성: 노출된 리소스에 `PUT`, `PATCH`, `DELETE`, 일반 `POST` 요청이 거절되는지, 재고, 결제 상태, 하위 주문 상태, 멱등키, 변경 이력이 쓰기용 리소스로 노출되지 않는지, 재고와 배송과 멱등키와 변경 이력이 `/api` 목록에 없는지 확인한다(ADR-0008 규칙 1)
- [ ] T013 [P] `테스트/integration/SecurityRulesTest.java` 작성: 로그인하지 않은 요청은 로그인으로 보내고, "고객 계정은 판매 기능을, 판매자 계정은 장바구니와 주문 기능을 쓸 수 없다."를 확인하며, 사이트 간 요청 위조 방어 토큰 없는 상태 변경 요청은 거절한다(FR-002a, 헌법 원칙 II)
- [ ] T014 [P] `테스트/unit/MoneyTest.java` 작성: 원 단위 정수만 쓰고 부동소수점을 쓰지 않는다. 더하기와 곱하기, 음수 거절, 오버플로 거절(헌법 원칙 IX)
- [ ] T015 [P] `테스트/integration/IdempotencyServiceTest.java` 작성: "(범위, 키)가 유일하다. 같은 키에 내용이 다르면 거절한다." 같은 키와 같은 내용은 저장된 결과를 그대로 돌려주고, 같은 키에 다른 내용은 거절(422)하며, 같은 키의 동시 요청 100건 중 하나만 처리한다(ADR-0002)
- [ ] T016 [P] `테스트/integration/AuditLogTest.java` 작성: "수정과 삭제는 하지 않는다. 개인정보와 카드 정보는 담지 않는다." 이전 상태는 선택(첫 기록은 없음) (FR-016)
- [ ] T017 [P] `테스트/integration/FileStorageTest.java` 작성: "파일은 서버가 정한 이름으로 비공개 위치에 저장하고 허용 형식과 크기를 검사한다." 허용되지 않는 형식과 큰 파일은 거절한다
- [ ] T018 [P] `테스트/integration/ErrorResponseTest.java` 작성: 권한 없는 접근(다른 고객의 주문, 다른 판매자의 하위 주문)은 존재 여부를 알리지 않도록 404, 허용되지 않는 상태 변경은 409, 같은 멱등키에 다른 내용은 422이며, 안내 문구는 한글이다

### 기반 구현

- [ ] T019 `src/main/resources/db/migration/V1__common.sql` 작성: `idempotency_key`, `audit_log`와 열거형 타입(`specs/001-multiseller-commerce-core/schema.sql`을 따른다). 멱등키의 `(scope, idem_key)` 유일 제약 포함
- [ ] T020 [P] `…/common/Money.java` 작성: 원 단위 정수(`long`)를 담는 값 객체(T014를 통과시킨다)
- [ ] T021 [P] `…/common/TimeProvider.java` 작성: 시각 공급자(테스트에서 시간을 고정할 수 있게 한다)
- [ ] T022 [P] `…/common/error/` 작성: 업무 예외 타입과 `GlobalExceptionHandler`(404, 409, 422 응답과 한글 문구, T018을 통과시킨다)
- [ ] T023 [P] `…/common/idempotency/` 작성: `IdempotencyKey` 엔티티(범위, 키, 요청 내용 해시, 처리 상태(처리 중, 완료), 저장된 결과(선택), 만료 시각), `IdempotencyRepository`, `IdempotencyService`. "처리 중 → 완료"만 허용하고 완료된 키는 만료 시각이 지나면 정리 작업이 지운다(T015를 통과시킨다)
- [ ] T024 [P] `…/common/audit/` 작성: `AuditLog` 엔티티(대상 종류, 대상 식별자, 이전 상태(선택), 이후 상태, 행위자, 발생 시각), `AuditLogRepository`(추가만, 수정과 삭제 메서드 없음), `AuditService`(T016을 통과시킨다)
- [ ] T025 [P] `…/common/storage/` 작성: `FileStoragePort` 인터페이스, `LocalDiskFileStorage`(비공개 위치, 서버가 정한 이름, 형식과 크기 검사, T017을 통과시킨다)
- [ ] T026 `…/web/security/SecurityConfig.java` 작성: 스프링 시큐리티, 사이트 간 요청 위조 방어, `PasswordEncoder`(솔트가 적용된 해시), 역할(고객, 판매자, 운영자), 메서드 보안 활성화, 웹훅 경로는 서명 검증으로 제외(T013을 통과시킨다)
- [ ] T027 [P] `…/common/config/RepositoryRestConfig.java` 작성: 어노테이션을 붙인 리포지토리만 노출, 기본 쓰기 메서드 비노출, 내부 식별자와 비밀번호 해시 필드 비노출(T012를 통과시킨다)
- [ ] T028 [P] `…/client/` 작성: 컨텍스트별 API 클라이언트 인터페이스(`AccountApiClient`, `ProductApiClient`, `OrderApiClient`, `PaymentApiClient`)와 같은 프로세스 호출 구현의 골격, 오류 응답을 화면용 예외로 바꾸는 규칙(ADR-0008)
- [ ] T029 [P] `src/main/resources/templates/layout.html`, `error.html` 작성: 공통 화면 틀과 한글 오류 화면
- [ ] T030 `specs/001-multiseller-commerce-core/contracts/rest-api.md` 골격 작성: 공통 규칙(경로 `/api`, 응답 모양, 오류 응답 404/409/422, 멱등키 헤더, 링크 규칙, 읽기 전용 프로젝션). 각 사용자 스토리가 자기 API 섹션을 추가한다(ADR-0008 후속)
- [ ] T031 `specs/001-multiseller-commerce-core/contracts/web-routes.md`를 화면 MVC 계층의 화면 경로 문서로 정리한다(REST API 경로와 구분, ADR-0008 후속)

**체크포인트**: 기반이 준비됨. 이제 사용자 스토리를 병렬로 시작할 수 있다.

---

## 3단계: 사용자 스토리 1 - 판매자가 입점하고 상품을 등록한다 (우선순위: P1) 🎯 MVP

**목표**: 판매자가 사업자 정보와 증빙 서류로 입점을 신청하고, 운영자가 승인하면 옵션과 재고가 있는 상품을 등록하고 수정하고 판매를 중지할 수 있다.

**독립 시험**: 판매자 계정 하나로 입점 신청, 운영자 승인, 상품 등록까지 해 보고, 등록한 상품이 고객 화면의 상품 목록에 보이면 가치가 전달된 것이다(SC-001).

### 사용자 스토리 1 테스트 (먼저 작성하고 실패를 확인한다) ⚠️

- [ ] T032 [US1] `contracts/rest-api.md`에 계정(판매자 정보, 입점 서류)과 상품 API 섹션 추가: 읽기 리소스, 동작(입점 신청, 승인, 거절, 정지, 상품 등록, 수정, 판매 중지) 경로와 요청·응답과 오류
- [ ] T033 [P] [US1] `테스트/integration/SellerApplicationTest.java` 작성(FR-002, FR-002b~FR-002d): 개인사업자와 법인사업자 신청 접수, 필수 항목 또는 서류 누락 시 접수하지 않고 빠진 항목 안내, 같은 사업자등록번호는 "이미 등록된 사업자"로 거절, "법인사업자는 법인명과 법인등록번호가 필수이고 개인사업자는 비어 있어야 한다.", 대표자 주민등록번호 필드가 존재하지 않음, "사업자 등록이 없는 개인은 입점할 수 없다.", 서류 형식과 크기 초과 거절
- [ ] T034 [P] [US1] `테스트/integration/SellerApprovalTest.java` 작성(FR-002): "신청 → 승인 또는 거절, 승인 → 정지, 정지 → 승인. 거절은 종료 상태다." 허용되지 않는 전이는 409, 운영자만 승인과 거절 가능
- [ ] T035 [P] [US1] `테스트/integration/SellerDocumentAccessTest.java` 작성(FR-002d): "판매자 본인과 운영자만 조회할 수 있다." 다른 판매자와 고객은 404
- [ ] T036 [P] [US1] `테스트/integration/ProductRegistrationTest.java` 작성(FR-003, FR-003a): 승인 전 등록 거절, 승인 후 등록, "이름은 비어 있을 수 없다.", "가격은 0보다 크다.", "한 상품 안에서 옵션 값 조합은 유일하다.", "옵션이 없는 상품은 판매 단위가 하나다.", 판매 단위 등록 시 재고 개체 생성
- [ ] T037 [P] [US1] `테스트/integration/SellerProductAccessTest.java` 작성(FR-004): 판매자 A는 A의 상품만 조회하고 고칠 수 있고 B의 상품은 404, 판매 중지한 상품과 정지·거절 판매자의 상품은 고객 목록에서 사라진다(SC-006)
- [ ] T038 [P] [US1] `테스트/integration/StockRegistrationTest.java` 작성: "판매 단위마다 하나다(유일 제약). 가용 수량은 0 이상이며 데이터베이스 제약으로 음수를 막는다." 재고 등록과 수정, 음수 입력 거절
- [ ] T039 [P] [US1] `테스트/e2e/SellerOnboardingFlowTest.java` 작성: 입점 신청 → 운영자 승인 → 옵션이 있는 상품 등록 → 고객 목록 노출(SC-001)

### 사용자 스토리 1 구현

- [ ] T040 [US1] `src/main/resources/db/migration/V2__account.sql` 작성: `account`(유형과 이메일의 `uq_account_type_email` 유일), `seller_profile`(사업자등록번호 유일, `ck_seller_corporate_fields` 검사 제약), `seller_document`
- [ ] T041 [US1] `src/main/resources/db/migration/V3__product_inventory.sql` 작성: `product`, `product_photo`, `product_variant`(`ck_variant_price_positive`, `uq_variant_product_options`), `stock`(`ck_stock_non_negative`, 판매 단위 유일)
- [ ] T042 [P] [US1] `…/account/` 엔티티 작성: `Account`(유형(고객, 판매자, 운영자), 이메일, 비밀번호 해시, 상태. "(유형, 이메일)이 유일하다. 비밀번호는 솔트가 적용된 해시만 저장한다."), `SellerProfile`(계정 참조, 사업자 유형(개인사업자, 법인사업자), 상호명, 사업자등록번호, 대표자명, 연락처, 사업장 주소, 통신판매업 신고번호, 법인명(선택, 법인만), 법인등록번호(선택, 법인만), 입점 상태(신청, 승인, 거절, 정지). "대표자 주민등록번호는 저장하지 않는다."), `SellerDocument`(판매자 정보 참조, 서류 종류, 파일 저장 위치, 제출 시각). 컨텍스트 사이 연관은 쓰지 않는다
- [ ] T043 [P] [US1] `…/account/repository/` 작성: `AccountRepository`, `SellerProfileRepository`(읽기 전용 리소스로 노출, 본인과 운영자만 조회하는 권한 조건), `SellerDocumentRepository`(노출하지 않거나 본인과 운영자 조건의 읽기 전용)
- [ ] T044 [US1] `…/account/service/SellerOnboardingService.java` 작성: 신청(계정, 판매자 정보, 서류를 한 번에 만든다), 승인, 거절, 정지, 정지 해제. "승인 상태가 아니면 상품을 등록할 수 없고, 정지·거절 판매자의 상품은 고객에게 보이지 않는다." 상태 변경은 감사 이력에 남긴다(T033, T034를 통과시킨다)
- [ ] T045 [US1] `…/account/api/SellerActionController.java` 작성: 입점 신청(서류 파일 올리기, `FileStoragePort` 사용), 운영자 승인·거절·정지 동작 엔드포인트. 쓰기용 리소스는 노출하지 않는다(T035를 통과시킨다)
- [ ] T046 [P] [US1] `…/product/` 엔티티 작성: `Product`(판매자 식별자, 이름, 설명(선택), 판매 상태(판매 중, 판매 중지). "하나 이상의 판매 단위를 가진다. 이름은 비어 있을 수 없다."), `ProductVariant`(상품 참조, 옵션 값 목록, 가격. "가격은 0보다 크다. 한 상품 안에서 옵션 값 조합은 유일하다. 옵션이 없는 상품은 판매 단위가 하나다."), `ProductPhoto`
- [ ] T047 [P] [US1] `…/inventory/` 엔티티와 리포지토리 작성: `Stock`(판매 단위 식별자, 가용 수량. "판매 단위마다 하나다(유일 제약). 가용 수량은 0 이상이며 데이터베이스 제약으로 음수를 막는다. 값은 재고 컨텍스트가 조건부 갱신으로만 바꾼다."), `StockRepository`(조건부 갱신 질의만)
- [ ] T048 [US1] `…/inventory/service/StockService.java` 작성: 재고 등록과 수정(REST로 노출하지 않고 상품 서비스가 서비스 인터페이스로 부른다)(T038을 통과시킨다)
- [ ] T049 [US1] `…/product/service/ProductService.java`와 `…/product/repository/ProductRepository.java` 작성: 상품과 옵션 조합 등록, 수정, 판매 중지, 판매자 본인 상품만 조회·수정, 사진 저장(`FileStoragePort`), 재고 등록은 `StockService` 호출(T036, T037을 통과시킨다)
- [ ] T050 [US1] `…/product/api/ProductActionController.java` 작성: 상품 등록, 수정, 판매 중지 동작 엔드포인트와 판매자용 읽기 리소스(본인 상품만)
- [ ] T051 [P] [US1] `…/client/` 구현 추가: `SellerApiClient`, `AdminApiClient`(같은 프로세스 호출), `ProductApiClient`의 판매자용 메서드
- [ ] T052 [US1] `…/web/auth/AuthController.java`와 `templates/auth/login.html` 작성: 로그인과 로그아웃 화면(고객, 판매자, 운영자 공용)
- [ ] T053 [P] [US1] `…/web/seller/SellerScreenController.java`와 `templates/seller/` 작성: 판매자 가입과 입점 신청(서류 올리기), 내 상품 목록, 상품과 옵션 등록·수정·판매 중지 화면
- [ ] T054 [P] [US1] `…/web/admin/AdminScreenController.java`와 `templates/admin/sellers.html` 작성: 입점 신청 목록과 서류 확인, 승인과 거절 화면
- [ ] T055 [US1] 사용자 스토리 1 통합 점검: T032~T039의 모든 테스트가 통과하는지 확인하고 `contracts/rest-api.md` 섹션을 실제 구현과 맞춘다

**체크포인트**: 이 시점에 사용자 스토리 1이 완전히 동작하고 독립적으로 시험할 수 있어야 한다.

---

## 4단계: 사용자 스토리 2 - 고객이 가입하고 상품을 찾아본다 (우선순위: P1)

**목표**: 고객이 이메일로 가입하고 로그인하며, 로그인하지 않아도 상품 목록과 상세를 보고 이름으로 검색하고 판매자별로 모아 볼 수 있다.

**독립 시험**: 등록된 상품이 몇 개 있는 상태에서 고객이 가입, 로그인, 검색, 상세 보기를 해 본다.

### 사용자 스토리 2 테스트 (먼저 작성하고 실패를 확인한다) ⚠️

- [ ] T056 [US2] `contracts/rest-api.md`에 상품 조회 API(검색 쿼리 `q`, 판매자별 모아보기, 페이지, 판매 중만) 섹션 추가
- [ ] T057 [P] [US2] `테스트/integration/CustomerSignupTest.java` 작성(FR-001): 이메일과 비밀번호로 가입, 이미 가입된 이메일은 "이미 가입된 이메일" 안내, 형식 오류는 항목별 안내, 같은 이메일로 판매자 계정을 따로 만들 수 있음("(유형, 이메일)이 유일하다."), 비밀번호가 해시로만 저장됨
- [ ] T058 [P] [US2] `테스트/integration/ProductSearchTest.java` 작성(FR-005): 판매 중이고 승인된 판매자의 상품만 보임, 이름 일부로 검색, 판매자별 모아보기, 로그인 없이 조회 가능, 판매 중지·거절·정지 상품 주소로 직접 들어오면 404
- [ ] T059 [P] [US2] `테스트/integration/SoldOutDisplayTest.java` 작성: 특정 옵션 조합만 품절이면 그 조합만 선택 불가이고 나머지는 선택 가능, 모든 조합이 품절이면 품절 표시와 담기 막힘
- [ ] T060 [P] [US2] `테스트/e2e/CustomerBrowseFlowTest.java` 작성: 가입 → 로그인 → 검색 → 상세 보기

### 사용자 스토리 2 구현

- [ ] T061 [US2] `…/account/service/AccountService.java` 작성: 고객 가입(고객 유형 계정, 솔트가 적용된 해시, 유형별 이메일 유일 검사)(T057을 통과시킨다)
- [ ] T062 [US2] `…/account/api/` 에 고객 가입 동작 엔드포인트 추가(공개 경로, 사이트 간 요청 위조 방어 적용)
- [ ] T063 [US2] `…/product/repository/ProductRepository.java`에 검색 질의 추가(이름 일부, 판매자별, 판매 중만, 승인된 판매자만)와 읽기 전용 조회 프로젝션(공개용 식별자와 필요한 필드만)(T058을 통과시킨다)
- [ ] T064 [P] [US2] `…/product/service/ProductQueryService.java` 작성: 상품 목록과 상세 조립, 판매 단위별 선택 가능 여부(재고 서비스에서 가용 여부만 조회)(T059를 통과시킨다)
- [ ] T065 [P] [US2] `…/client/` 구현 추가: `ProductApiClient`의 고객용 조회 메서드, `AccountApiClient`의 가입 메서드
- [ ] T066 [P] [US2] `…/web/product/ProductScreenController.java`와 `templates/product/list.html`, `detail.html`, `seller-products.html` 작성: 상품 목록, 검색, 판매자별 모아보기, 상세와 옵션 선택, 품절 조합 표시
- [ ] T067 [P] [US2] `…/web/auth/SignupController.java`와 `templates/auth/signup.html` 작성: 고객 가입 화면
- [ ] T068 [US2] 사용자 스토리 2 통합 점검: T056~T060의 모든 테스트가 통과하는지 확인하고 `contracts/rest-api.md`를 맞춘다

**체크포인트**: 이 시점에 사용자 스토리 1과 2가 함께 동작하고 각각 독립적으로 시험할 수 있어야 한다.

---

## 5단계: 사용자 스토리 3 - 고객이 장바구니에 담고 결제해 주문한다 (우선순위: P1)

**목표**: 고객이 여러 판매자의 상품을 한 장바구니에 담아 한 번에 주문한다. 결제를 시작하면 재고가 먼저 확보되고, 결제 성공 시 주문이 확정되며, 실패나 시간 초과 시 재고와 주문이 되돌아간다. 같은 결제 요청이 여러 번 와도 결제는 한 번만 일어난다. 즉시 결제와 입금 대기(가상계좌)를 모두 지원한다.

**독립 시험**: 상품이 등록된 상태에서 담기, 주문, 결제를 끝까지 해 보고 성공과 실패와 중복 요청을 각각 확인한다.

### 사용자 스토리 3 테스트 (먼저 작성하고 실패를 확인한다) ⚠️

- [ ] T069 [US3] `contracts/rest-api.md`에 주문(장바구니, 주문, 하위 주문 읽기 리소스와 담기, 결제 시작 동작)과 결제(결제 조회, 승인 확인, 웹훅) API 섹션 추가. `contracts/payment-gateway-port.md`와 어긋나지 않게 한다
- [ ] T070 [P] [US3] `테스트/support/FakePaymentGateway.java` 작성: 결제 포트의 가짜 구현이 `payment-gateway-port.md`의 시나리오(성공, 실패, 시간 초과(결과 모름), 같은 멱등키 재시도, 금액 불일치, 웹훅 중복, 웹훅 서명 오류, 기한 후 입금)를 낼 수 있게 한다
- [ ] T071 [P] [US3] `테스트/integration/CartTest.java` 작성(FR-006): 옵션 조합을 골라 담기, 같은 조합은 수량을 합치기, "(고객, 판매 단위)가 유일하다. 수량은 1 이상이다.", 판매 중지·품절 조합은 담기 거절, "재고보다 많은 수량으로는 주문을 시작할 수 없다."
- [ ] T072 [P] [US3] `테스트/integration/StockReservationConcurrencyTest.java` 작성(SC-004, ADR-0001): 재고 1개인 판매 단위에 100건이 동시에 주문해도 성공은 정확히 1건, 조건부 갱신으로만 차감
- [ ] T073 [P] [US3] `테스트/integration/StockReservationStateTest.java` 작성: "유지 → 확정 또는 반환. 확정과 반환은 종료 상태다." 확정과 반환은 한 번만 일어남, 만료 시각은 결제 방식별 설정값(즉시 결제 15분, 입금 대기 24시간)
- [ ] T074 [P] [US3] `테스트/integration/CheckoutTest.java` 작성(FR-007, FR-008, FR-011, FR-011a): 두 판매자의 상품을 주문하면 주문(결제 대기)과 판매자별 하위 주문 2개와 재고 확보와 결제(준비)가 만들어지고, 결제 성공 시 주문이 확정되며, 확정 전에는 하위 주문이 판매자에게 보이지 않고, 재고 부족이면 전체를 되돌린다
- [ ] T075 [P] [US3] `테스트/integration/PaymentIdempotencyTest.java` 작성(SC-003, FR-009): 같은 멱등키 요청을 10번 보내도 결제는 정확히 1번, 같은 키에 다른 내용은 거절, 중복 클릭과 재시도가 겹쳐도 같은 결과
- [ ] T076 [P] [US3] `테스트/integration/PaymentVerificationTest.java` 작성(FR-010): 서버가 대행사에 직접 조회해 상태, 주문 번호, 금액을 대조하고, 금액이나 주문 번호가 맞지 않으면 확정하지 않고 오류로 기록하며, 결제 시작 뒤 가격이 바뀐 상품이 있으면 새 금액으로 다시 확인하게 한다
- [ ] T077 [P] [US3] `테스트/integration/PaymentStateTest.java` 작성: 결제 상태 전이(준비 → 승인 대기/입금 대기/실패, 승인 대기 → 성공/실패/만료, 입금 대기 → 성공/만료)와 "한 주문에 성공 결제는 하나만 있다(유일 제약)." 성공, 실패, 만료는 종료 상태
- [ ] T078 [P] [US3] `테스트/integration/PaymentFailureTest.java` 작성(UC-04 6a, 6c): 결제 실패 시 재고 반환, 주문 취소됨, 하위 주문 취소, 청구 없음. 시간 초과로 결과를 모르면 조회로 확인하기 전까지 재고를 반환하지 않는다
- [ ] T079 [P] [US3] `테스트/integration/DepositWaitTest.java` 작성(FR-008a, UC-05): 가상계좌 발급, 기한 안 입금은 웹훅을 서명 검증하고 이벤트 식별자로 중복 제거해 확정, 기한 경과는 주문과 하위 주문 자동 취소와 재고 반환, 기한 후 입금은 확정하지 않고 "확인 필요"로 기록
- [ ] T080 [P] [US3] `테스트/integration/ReservationExpiryJobTest.java` 작성(SC-005, UC-17, UC-18): 만료된 재고 확보를 반환하고 중복 실행해도 한 번만 반환(건너뜀 잠금), 결제는 성공했는데 재고 확보 유지 시간이 이미 지났으면 주문을 확정하지 않고 "확인 필요"로 기록(UC-04 7a)
- [ ] T081 [P] [US3] `테스트/integration/WebhookSecurityTest.java` 작성: 서명 검증에 실패하면 400으로 거절하고 아무것도 바꾸지 않으며, 같은 이벤트가 여러 번 와도 한 번만 처리하고 항상 정상 응답한다
- [ ] T082 [P] [US3] `테스트/integration/SensitiveDataLogTest.java` 작성(FR-015): 카드번호 원문이 저장과 로그에 없고 개인정보가 로그에 남지 않는다
- [ ] T083 [P] [US3] `테스트/integration/StatusAuditTest.java` 작성(FR-016): 결제, 주문, 하위 주문, 재고 확보의 상태가 바뀔 때마다 감사 이력이 한 행씩 추가된다
- [ ] T084 [P] [US3] `테스트/integration/OrderAccessTest.java` 작성(FR-014, SC-006): 고객은 본인 주문과 하위 주문별 상태만 조회하고 다른 고객의 주문은 404
- [ ] T085 [P] [US3] `테스트/e2e/PurchaseFlowTest.java` 작성(SC-002): 상품 검색 → 담기 → 주문 → 결제 성공 → 주문 확정 → 주문 조회
- [ ] T086 [P] [US3] `테스트/integration/ReviewOrderListTest.java` 작성(UC-16): 운영자가 "확인 필요" 주문과 사유를 조회할 수 있고, 고객과 판매자는 접근할 수 없다

### 사용자 스토리 3 구현

- [ ] T087 [US3] `src/main/resources/db/migration/V4__order.sql` 작성: `cart_item`(`uq_cart_customer_variant`, `ck_cart_quantity_positive`), `customer_order`, `sub_order`, `sub_order_item`(`ck_sub_order_item_quantity_positive`)
- [ ] T088 [US3] `src/main/resources/db/migration/V5__stock_reservation.sql` 작성: `stock_reservation`(`ck_reservation_quantity_positive`, 만료 처리용 `(status, expires_at)` 인덱스)
- [ ] T089 [US3] `src/main/resources/db/migration/V6__payment.sql` 작성: `payment`, `payment_transaction`, 한 주문에 성공 결제 하나인 부분 유일 인덱스 `uq_payment_one_success_per_order`(`specs/001-multiseller-commerce-core/schema.sql` 끝 부분)
- [ ] T090 [P] [US3] `…/order/` 엔티티 작성: `CartItem`(고객 식별자, 판매 단위 식별자, 수량), `CustomerOrder`(고객 식별자, 총 금액, 상태(결제 대기, 확정, 취소됨, 확인 필요), 배송지 정보. "총 금액은 하위 주문 금액의 합과 같다."), `SubOrder`(주문 참조, 판매자 식별자, 금액, 배송 상태(발송 전, 배송 중, 배송 완료, 구매 확정, 취소), 구매 확정 시각(선택), 취소 시각(선택)), `SubOrderItem`("주문 당시 단가를 복사해 두어 이후 가격이 바뀌어도 주문 금액이 달라지지 않는다.")
- [ ] T091 [P] [US3] `…/inventory/` 엔티티 작성: `StockReservation`(주문 식별자, 판매 단위 식별자, 수량, 만료 시각, 상태(유지, 확정, 반환). "유지 상태의 수량은 이미 재고에서 차감되어 있다. … 반환은 한 번만 일어난다.")
- [ ] T092 [P] [US3] `…/payment/` 엔티티 작성: `Payment`(주문 식별자, 결제 방식(즉시 결제, 입금 대기), 금액, 상태(준비, 승인 대기, 입금 대기, 성공, 실패, 만료), 대행사 결제 키(선택), 입금 기한(선택, 입금 대기 방식만). "청구 금액은 주문 총액과 같아야 한다."), `PaymentTransaction`("수정하거나 지우지 않는다. 환불은 새 행을 추가한다.")
- [ ] T093 [US3] `…/inventory/repository/StockReservationRepository.java`와 `StockRepository` 조건부 갱신 질의 작성: 차감(`WHERE 가용 수량 >= 요청 수량`), 확정과 반환(`WHERE 상태 = 유지`), 만료 조회(`FOR UPDATE SKIP LOCKED`)(T072, T073을 통과시킨다)
- [ ] T094 [US3] `…/inventory/service/StockService.java`에 재고 선점, 확정, 반환, 만료 처리 추가(REST로 노출하지 않음, 상태 변경은 감사 이력에 남긴다)
- [ ] T095 [P] [US3] `…/order/service/CartService.java`와 `…/order/repository/CartItemRepository.java` 작성(T071을 통과시킨다)
- [ ] T096 [P] [US3] `…/payment/service/port/PaymentGateway.java` 작성: 결제 포트(준비, 승인, 조회, 환불)
- [ ] T097 [US3] `…/payment/service/PaymentService.java`와 `…/payment/repository/PaymentRepository.java` 작성: 결제 준비, 승인 확인(서버 직접 조회와 금액 대조), 상태 전이(T077), 웹훅 반영(이벤트 식별자로 중복 제거), 거래 내역 추가(T076, T078, T079, T081을 통과시킨다)
- [ ] T098 [P] [US3] `…/payment/adapter/TossPaymentGateway.java` 작성: 토스페이먼츠 호출 구현(멱등키 헤더, 시간 초과 처리). 카드번호는 서버를 지나가지 않는다. 테스트 키는 설정값으로만 받고 저장소에 커밋하지 않는다(헌법 원칙 II)
- [ ] T099 [US3] `…/order/service/OrderService.java`와 `…/order/repository/OrderRepository.java`, `SubOrderRepository.java` 작성: 주문과 하위 주문 생성, 주문 확정, 결제 실패·만료 시 주문과 하위 주문 취소("주문이 결제 대기에서 취소됨으로 바뀌면 속한 하위 주문도 모두 취소로 바꾼다(같은 트랜잭션)."), 확인 필요 기록, 고객과 판매자별 조회 제한(T074, T084를 통과시킨다)
- [ ] T100 [US3] `…/order/service/CheckoutCoordinator.java` 작성: 멱등키 확인 → 재고 선점 → 주문과 하위 주문과 결제 생성 → 결제 준비를 서비스 인터페이스로 순서대로 호출하고, 결제 결과에 따라 확정 또는 취소를 지시한다. 단계마다 멱등하게 한다(T075, T078을 통과시킨다)
- [ ] T101 [US3] `…/common/scheduler/ScheduledJobs.java` 작성: 1분마다 만료된 재고 확보 반환, 입금 기한 만료 주문 취소(UC-17, UC-18). 건너뜀 잠금으로 중복 실행을 막는다(T080을 통과시킨다)
- [ ] T102 [US3] `…/order/api/OrderActionController.java`와 읽기 리소스 어노테이션 작성: 담기, 결제 시작 동작과 장바구니, 주문, 하위 주문 읽기 리소스(본인 것만, 판매자는 확정된 주문의 하위 주문만). 상태 필드는 쓰기 불가
- [ ] T103 [P] [US3] `…/payment/api/PaymentActionController.java`, `WebhookController.java` 작성: 본인 결제 조회, 승인 확인 동작, 웹훅 수신(서명 검증, 로그인 없이 호출)
- [ ] T104 [P] [US3] `…/order/service/ReviewOrderQueryService.java`와 `…/order/api/` 운영자용 읽기 리소스 작성: 확인 필요 주문과 사유 조회(T086을 통과시킨다)
- [ ] T105 [P] [US3] `…/client/` 구현 추가: `OrderApiClient`(장바구니, 결제 시작, 주문 조회), `PaymentApiClient`(승인 확인)
- [ ] T106 [P] [US3] `…/web/order/CartScreenController.java`, `CheckoutScreenController.java`, `OrderScreenController.java`와 `templates/order/` 작성: 장바구니, 주문 확인(금액과 배송지와 결제 방식 선택), 결제 결과(성공, 실패, 가상계좌 안내), 내 주문과 하위 주문별 상태 화면, 변경된 가격 안내
- [ ] T107 [P] [US3] `…/web/admin/ReviewOrderScreenController.java`와 `templates/admin/review-orders.html` 작성: 운영자 확인 필요 주문 화면
- [ ] T108 [US3] 사용자 스토리 3 통합 점검: T069~T086의 모든 테스트가 통과하는지 확인하고 `contracts/rest-api.md`와 `quickstart.md`의 "결제 시작과 성공", "결제 실패와 만료" 시나리오를 맞춘다

**체크포인트**: 이 시점에 사용자 스토리 1~3이 함께 동작하고, 구매 흐름의 핵심이 끝까지 검증된다.

---

## 6단계: 사용자 스토리 4 - 판매자가 자기 주문을 확인하고 배송을 처리한다 (우선순위: P2)

**목표**: 판매자가 자기 하위 주문만 확인하고 송장을 입력하고 배송 완료로 바꾸며, 고객은 상태를 보고 구매 확정을 누른다(누르지 않으면 7일 뒤 자동 확정).

**독립 시험**: 확정된 주문이 있는 상태에서 판매자가 배송 처리를 하고 고객이 상태 변화를 확인한 뒤 구매 확정을 한다.

### 사용자 스토리 4 테스트 (먼저 작성하고 실패를 확인한다) ⚠️

- [ ] T109 [US4] `contracts/rest-api.md`에 배송 처리(송장 입력, 배송 완료)와 구매 확정 동작 섹션 추가
- [ ] T110 [P] [US4] `테스트/integration/SubOrderStateTransitionTest.java` 작성(FR-013): 허용 목록(발송 전 → 배송 중/취소, 배송 중 → 배송 완료, 배송 완료 → 구매 확정, 구매 확정과 취소는 종료)대로만 바뀌고 나머지는 409, 상태 변경은 `WHERE 상태 = 현재 상태` 조건부 갱신으로 먼저 처리된 요청만 반영
- [ ] T111 [P] [US4] `테스트/integration/ShipSubOrderTest.java` 작성(FR-012, FR-011a): 송장 입력으로 "발송 전"에서 "배송 중"이 되고 고객 화면에 반영되며, 주문이 확정된 하위 주문만 처리할 수 있고(결제 대기, 취소됨, 확인 필요는 거절), 송장 번호가 비어 있으면 거절, 같은 주문의 다른 판매자 하위 주문은 그대로, 다른 판매자의 하위 주문은 404(SC-006)
- [ ] T112 [P] [US4] `테스트/integration/DeliveryInfoTest.java` 작성: "하위 주문 하나에 최대 하나다(유일 제약). 송장 입력과 하위 주문의 배송 상태 변경(…)은 같은 트랜잭션에서 처리하며, 하위 주문이 이미 취소되었으면 송장을 저장하지 않는다."(괄호 안은 "발송 전"에서 "배송 중"으로의 변경) 배송 완료 처리 때 배송 완료 시각 기록
- [ ] T113 [P] [US4] `테스트/integration/ConfirmPurchaseTest.java` 작성(FR-012, UC-08): 고객이 구매 확정을 누르면 구매 확정 상태와 시각이 기록되고, 배송 완료가 아니거나 이미 확정·취소된 하위 주문은 거절
- [ ] T114 [P] [US4] `테스트/integration/AutoConfirmJobTest.java` 작성(UC-19): 배송 완료 후 7일(설정값)이 지나면 자동 구매 확정, 이미 확정된 건은 건드리지 않음, 설정값을 바꿀 수 있음
- [ ] T115 [P] [US4] `테스트/integration/StatusVisibilityTest.java` 작성(SC-007): 판매자의 발송 처리 결과가 고객 화면에 1분 안에 보임
- [ ] T116 [P] [US4] `테스트/e2e/DeliveryFlowTest.java` 작성: 확정된 주문 → 송장 입력 → 배송 완료 → 고객 구매 확정, 두 판매자 하위 주문의 독립 처리

### 사용자 스토리 4 구현

- [ ] T117 [US4] `src/main/resources/db/migration/V7__delivery.sql` 작성: `delivery`(하위 주문 식별자 유일)
- [ ] T118 [P] [US4] `…/delivery/` 엔티티와 리포지토리 작성: `Delivery`(하위 주문 식별자, 택배사, 송장 번호, 배송 완료 시각(선택)), `DeliveryRepository`
- [ ] T119 [US4] `…/delivery/service/DeliveryService.java` 작성: 송장 저장, 배송 완료 시각 기록(REST로 노출하지 않고 주문 서비스가 부른다)(T112를 통과시킨다)
- [ ] T120 [US4] `…/order/service/OrderService.java`에 발송 처리, 배송 완료 처리, 구매 확정 추가: 조건부 갱신으로 상태 전이, 판매자 본인과 확정된 주문 검사, 배송 서비스 호출, 감사 이력(T110, T111, T113을 통과시킨다)
- [ ] T121 [US4] `…/common/scheduler/ScheduledJobs.java`에 배송 완료 후 자동 구매 확정 추가(T114를 통과시킨다)
- [ ] T122 [US4] `…/order/api/OrderActionController.java`에 발송 처리, 배송 완료 처리, 구매 확정 동작 엔드포인트 추가
- [ ] T123 [P] [US4] `…/client/` 구현 추가: `OrderApiClient`의 판매자 배송 처리와 고객 구매 확정 메서드
- [ ] T124 [P] [US4] `…/web/seller/SellerOrderScreenController.java`와 `templates/seller/orders.html`, `order-detail.html` 작성: 판매자 하위 주문 목록과 상세, 송장 입력, 배송 완료 처리(T115의 1분 이내 반영을 지킨다)
- [ ] T125 [P] [US4] 고객 화면 갱신 `templates/order/detail.html`: 하위 주문별 상태, 송장 정보, 배송 완료 후 구매 확정 버튼
- [ ] T126 [US4] 사용자 스토리 4 통합 점검: T109~T116의 모든 테스트가 통과하는지 확인하고 문서를 맞춘다

**체크포인트**: 이 시점에 사용자 스토리 1~4가 모두 동작한다.

---

## 7단계: 사용자 스토리 5 - 고객이 발송 전에 하위 주문을 취소한다 (우선순위: P2)

**목표**: 고객이 결제 후 발송 전의 하위 주문을 하위 주문 단위로 취소하면 재고가 돌아오고 그 금액이 전액 환불된다.

**독립 시험**: 확정된 주문에서 발송 전 하위 주문을 취소해 재고 복구와 환불 금액을 확인한다.

### 결정 필요

- [ ] T127 [US5] **[결정 필요]** 발송 전 취소로 재고가 돌아올 때 재고 확보 상태를 어떻게 할지 정한다. 현재 데이터 모델은 "확정과 반환은 종료 상태다."이고 취소는 재고 가용 수량만 더하므로 재고 확보 상태는 "확정"으로 남는다. 이대로 두거나 취소 전용 상태를 더하는 중 하나를 정해 `data-model.md`, `diagrams/state-machines.puml`을 맞춘다. 정하기 전에는 T134~T136을 시작하지 않는다

### 사용자 스토리 5 테스트 (먼저 작성하고 실패를 확인한다) ⚠️

- [ ] T128 [US5] `contracts/rest-api.md`에 취소 동작(멱등키 헤더 필수)과 환불 결과 섹션 추가
- [ ] T129 [P] [US5] `테스트/integration/CancelSubOrderTest.java` 작성(FR-013a): 발송 전 하위 주문 취소 시 재고 가산과 하위 주문 금액 전액 환불, 한 주문의 다른 판매자 하위 주문은 그대로 진행, 모든 하위 주문이 취소되면 주문도 취소됨, 이미 배송 중인 하위 주문은 "발송된 주문은 취소할 수 없습니다"로 거절, 다른 고객의 하위 주문은 404
- [ ] T130 [P] [US5] `테스트/integration/CancelIdempotencyTest.java` 작성(SC-009, FR-013b): 같은 취소 요청을 10번 보내도 환불은 정확히 한 번이고 재고가 100% 되돌아온다
- [ ] T131 [P] [US5] `테스트/integration/CancelShipRaceTest.java` 작성(FR-013b): 고객의 취소와 판매자의 발송 처리가 동시에 들어오면 먼저 처리된 요청만 반영되고 다른 요청은 거절된다
- [ ] T132 [P] [US5] `테스트/integration/RefundLimitTest.java` 작성: "하위 주문별 환불 합계는 그 하위 주문 금액을 넘을 수 없다." 환불은 새 거래 내역 행으로만 추가되고 결제 상태는 바뀌지 않는다
- [ ] T133 [P] [US5] `테스트/integration/RefundFailureTest.java` 작성(UC-07 4a): 대행사 환불이 실패하거나 결과를 알 수 없으면 환불 대기로 기록하고 조회로 확인한 뒤 처리, 고객에게 처리 중임을 안내

### 사용자 스토리 5 구현

- [ ] T134 [US5] `…/order/service/OrderService.java`에 하위 주문 취소 추가: 발송 전에서만, 조건부 갱신으로 상태 변경, 취소 시각 기록, 모든 하위 주문 취소 시 주문 취소됨, 감사 이력(T129, T131을 통과시킨다)
- [ ] T135 [US5] `…/payment/service/PaymentService.java`에 환불 추가: 하위 주문 금액만큼 대행사에 환불 요청(멱등키 헤더), 환불 거래 내역 추가, 환불 합계 상한 검사, 실패·결과 모름 처리(T132, T133을 통과시킨다)
- [ ] T136 [US5] `…/order/service/CancelCoordinator.java` 작성: 멱등키 확인 → 취소 → 재고 가산 → 환불 순서를 서비스 인터페이스로 호출하고 단계마다 멱등하게 한다(T130을 통과시킨다)
- [ ] T137 [US5] `…/order/api/OrderActionController.java`에 취소 동작 엔드포인트 추가(본인 하위 주문만, 멱등키 필수)
- [ ] T138 [P] [US5] `…/client/` 구현 추가: `OrderApiClient`의 취소 메서드
- [ ] T139 [P] [US5] 고객 화면 갱신 `templates/order/detail.html`과 `…/web/order/OrderScreenController.java`: 발송 전 하위 주문에만 취소 버튼, 환불 결과와 처리 중 안내
- [ ] T140 [P] [US5] `테스트/e2e/CancelFlowTest.java` 작성: 두 판매자의 주문에서 한 판매자 하위 주문만 취소 → 환불과 재고 복구 확인
- [ ] T141 [US5] 사용자 스토리 5 통합 점검: T128~T133, T140이 통과하는지 확인하고 문서를 맞춘다

**체크포인트**: 이 시점에 모든 사용자 스토리가 독립적으로 동작한다.

---

## 8단계: 마무리와 공통 관심사

**목적**: 여러 스토리에 걸치는 점검과 정리

- [ ] T142 [P] `specs/001-multiseller-commerce-core/contracts/rest-api.md` 최종 정리와 REST 노출 범위 재점검(`RestExposureTest` 전체 통과, ADR-0008 규칙)
- [ ] T143 [P] 성능과 동시성 검증: 상품 목록·상세 응답 p95 1초 이내, 결제 시작 요청 p95 2초 이내(결제 대행사 응답 제외), 재고 1개에 동시 100건(SC-004) 결과를 기록한다(`docs/`에 결과 문서)
- [ ] T144 [P] 보안 점검(헌법 원칙 II, 품질 게이트): 결제, 인증, 개인정보를 다루는 코드와 설정을 점검하고, 비밀키가 저장소에 없는지, 로그에 개인정보가 없는지, 의존성에 치명적 취약점이 없는지 확인한다
- [ ] T145 [P] `specs/001-multiseller-commerce-core/quickstart.md`의 자동 검증 시나리오 표가 모두 테스트로 존재하는지 확인하고, 수동 확인 절차를 개발 서버에서 한 번 수행한다
- [ ] T146 [P] 한글과 쉬운 표현 점검(헌법 원칙 V, VI): 화면 문구, 오류 응답, 커밋과 PR 설명에 예외가 아닌 영문이 남아 있지 않은지 확인한다
- [ ] T147 [P] 변경 이력 점검(FR-016): 모든 상태 전이 경로에서 감사 이력이 남는지 `StatusAuditTest`를 확장해 확인한다
- [ ] T148 **[결정 필요]** 입점이 정지된 판매자의 진행 중 주문 처리 방식을 정하고 `spec.md`, `usecase-details.md`(UC-15)에 반영한 뒤 구현한다(명세에서 계획 단계로 미룬 항목)
- [ ] T149 **[결정 필요]** 거절된 판매자가 같은 사업자등록번호로 재신청하는 경우의 처리를 확정한다(명세 엣지 케이스의 기본값: 새 신청으로 받지 않고 기존 신청 상태를 안내)
- [ ] T150 `README.md` 갱신: 실행 방법, 테스트 방법, 문서 위치(명세, 계획, ADR, 다이어그램)
- [ ] T151 헌법 원칙 IX의 표현("배송 완료 후 정산")을 명세와 같은 "구매 확정 후 정산"으로 맞추는 헌법 개정 PR을 준비한다(ADR-0005 후속, 이슈와 브랜치를 따로 만든다)
- [ ] T152 `/speckit-taskstoissues`로 이 작업 목록을 하위 이슈로 만들지 정하고, 필요하면 실행한다(선택)
- [ ] T153 기능 브랜치를 `main`으로 합치는 PR을 `Closes #3`으로 열고, ADR PR(#5)과 헌법 개정 PR(#2)의 머지 순서를 정한다

---

## 의존 관계와 실행 순서

### 단계 의존 관계

- **설정(1단계)**: 의존 없음. 바로 시작한다. 단, T009(ADR 확정)가 끝나기 전에는 T020~T027과 US3를 시작하지 않는다.
- **기반(2단계)**: 설정이 끝나야 한다. **모든 사용자 스토리를 막는다.**
- **사용자 스토리(3~7단계)**: 모두 기반이 끝나야 한다.
  - US1과 US2는 기반 뒤에 병렬로 시작할 수 있다(US2는 시험 데이터로 US1 없이도 시험 가능, 둘을 합친 흐름은 e2e 테스트에서 확인).
  - US3은 상품(US1)과 고객 계정(US2)을 쓰므로 US1, US2 뒤에 한다.
  - US4는 확정된 주문이 필요하므로 US3 뒤에 한다.
  - US5는 US3(결제와 환불)과 US4(하위 주문 상태 전이) 뒤에 한다.
- **마무리(8단계)**: 원하는 스토리가 모두 끝난 뒤.

### 스토리 안의 순서

- 테스트를 먼저 쓰고 **실패를 확인한 뒤** 구현한다(헌법 원칙 I).
- 마이그레이션 → 엔티티 → 리포지토리 → 서비스 → REST API → API 클라이언트 → 화면 순서다.
- 스토리를 끝낼 때마다 통합 점검 작업과 PR(`Closes #번호`)로 마무리한다.

### 병렬 기회

- 설정의 [P] 작업(T002~T008)은 서로 병렬이다.
- 기반의 테스트 작업(T011~T018)은 모두 병렬이고, 구현 작업의 [P](T020~T025, T027~T029)도 병렬이다.
- 각 스토리의 테스트 작업은 대부분 [P]이다. 서로 다른 테스트 파일이기 때문이다.
- 같은 스토리 안에서 서로 다른 컨텍스트의 엔티티(예: T090, T091, T092)는 병렬이다.
- 화면과 API 클라이언트 작업([P])은 서비스가 정해진 뒤 병렬로 할 수 있다.

### 병렬 실행 예 (사용자 스토리 3 테스트)

```text
T070 FakePaymentGateway                 T071 CartTest
T072 StockReservationConcurrencyTest    T073 StockReservationStateTest
T074 CheckoutTest                       T075 PaymentIdempotencyTest
T076 PaymentVerificationTest            T077 PaymentStateTest
```

---

## 구현 전략

### 첫 시연 가능한 범위 (MVP)

1. 1단계 설정, 2단계 기반을 끝낸다.
2. **사용자 스토리 1**(판매자 입점과 상품 등록)을 끝낸다. 판매자가 상품을 올리고 고객 화면에 보이면 첫 가치가 전달된다.
3. 거기서 멈추고 시연한다.

### 판매가 가능한 최소 범위

실제로 사고팔 수 있으려면 **사용자 스토리 1~3(P1)** 이 모두 필요하다. 이 범위가 끝나면 결제와 재고의 핵심 규칙(재고 선점, 멱등 결제, 시간 초과 반환)이 끝까지 검증된다.

### 단계별 전달

1. 설정 + 기반 → 기반 완성
2. US1 → 시험 → PR (MVP)
3. US2 → 시험 → PR
4. US3 → 시험 → PR (판매 가능)
5. US4 → 시험 → PR
6. US5 → 시험 → PR
7. 마무리 → 기능 PR

### 위험과 주의

- **스프링 데이터 REST 노출 범위**: 쓰기 요청이 거절되는지, 남의 것이 보이지 않는지 테스트(T012, T142)로 항상 지킨다.
- **컨텍스트를 가로지르는 흐름**(결제 시작, 결제 성공, 발송 전 취소): 첫 구현은 한 트랜잭션이지만 단계마다 멱등하게 만들어 나중에 보상 처리로 바꿀 수 있게 한다(ADR-0007).
- **결정 필요 항목**(T009, T127, T148, T149)은 해당 작업을 시작하기 전에 정한다.
- 작업이 끝날 때마다 커밋하고, 스토리 단위로 PR을 연다(헌법 원칙 X).
