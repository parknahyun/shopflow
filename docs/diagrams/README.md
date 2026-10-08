# 다이어그램 다시 만들기

`specs/001-multiseller-commerce-core/diagrams/`의 이미지는 같은 폴더의 원본 파일에서 만든다. 원본을 고친 뒤 이 문서의 방법으로 이미지를 다시 만들고, 원본과 이미지를 함께 커밋한다.

## 파일 목록

| 원본 | 이미지 | 내용 |
|------|--------|------|
| `usecase.puml` | `usecase.png` | 유즈케이스 다이어그램 |
| `business-process.puml` | `business-process.png` | 전체 비즈니스 프로세스(정상 흐름) |
| `business-process-exceptions.puml` | `business-process-exceptions.png` | 주문·결제·재고 예외 흐름 |
| `business-process-cancel.puml` | `business-process-cancel.png` | 발송 전 취소와 환불 |
| `business-process-auto.puml` | `business-process-auto.png` | 시간 기반 자동 처리 |
| `sub-order-states.puml` | `sub-order-states.png` | 하위 주문 상태 전이 |
| `data-model-class.puml` | `data-model-class.png` | 데이터 모델 클래스 다이어그램(바운디드 컨텍스트) |
| `schema.dbml` | `schema-erd.png`, `schema-erd.svg` | 스키마 ERD |
| `schema.dbml` | `schema-erd-ie.puml`, `schema-erd-ie.png` | 스키마 ERD(정보공학 표기, 관계의 선택/필수와 컬럼의 NULL 허용 여부 표시) |
| `schema.dbml` | `../schema.sql` | PostgreSQL 참조 DDL (설계 단계용, 마이그레이션 아님) |

## 준비물

| 도구 | 용도 | 비고 |
|------|------|------|
| 자바 21 이상 | 플랜트유엠엘(PlantUML) 실행 | `java -version`으로 확인 |
| 플랜트유엠엘 실행 파일(`plantuml-1.2026.8.jar`) | `.puml`을 PNG로 변환 | 아래 내려받기 참고 |
| 그래프비즈(Graphviz) | 유즈케이스와 클래스 다이어그램 배치 | 설치 후 `dot.exe` 위치를 환경변수로 알려 준다 |
| 노드(npm 포함) | DBML 렌더러 실행 | `node --version`으로 확인 |
| 마이크로소프트 엣지(Edge) | SVG를 PNG로 캡처 | 윈도우 기본 설치 |

플랜트유엠엘 실행 파일은 저장소에 넣지 않는다. 프로젝트 밖의 폴더(예: `C:\tools\plantuml`)에 받아 둔다.

```powershell
# GitHub CLI로 받는 방법 (공식 릴리스)
gh release download v1.2026.8 --repo plantuml/plantuml --pattern "plantuml-1.2026.8.jar" --dir C:\tools\plantuml
```

`gh`가 없으면 플랜트유엠엘 공식 사이트의 내려받기 페이지에서 같은 이름의 파일을 받는다.

## PlantUML 이미지 만들기

저장소 루트에서 실행한다. 이미지는 원본 옆에 같은 이름의 `.png`로 만들어진다.

```powershell
# 그래프비즈 위치를 알려 준다 (설치 경로가 다르면 바꾼다)
$env:GRAPHVIZ_DOT = "C:\Program Files\Graphviz\bin\dot.exe"

java "-Dfile.encoding=UTF-8" -jar C:\tools\plantuml\plantuml-1.2026.8.jar `
  -charset UTF-8 "-SdefaultFontName=Malgun Gothic" "-Sdpi=130" -tpng `
  specs/001-multiseller-commerce-core/diagrams/*.puml
```

- 한 파일만 다시 만들려면 마지막 줄을 그 파일 경로로 바꾼다.
- `-SdefaultFontName`과 `-Sdpi`는 한글 글꼴과 해상도를 정한다. 원본 파일에는 넣지 않았으므로 항상 명령에서 지정한다.
- 변환 후 이미지를 열어 한글이 깨지지 않았는지 확인한다.

## ERD 이미지 만들기

`schema.dbml`에서 SVG와 PNG를 만든다. 저장소 루트에서 실행한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File docs\diagrams\render-erd.ps1
```

- 결과는 `specs/001-multiseller-commerce-core/diagrams/schema-erd.svg`와 `schema-erd.png`에 만들어진다.
- 처음 실행하면 npm이 DBML 렌더러(`@softwaretechnik/dbml-renderer`)를 내려받아 시간이 조금 걸린다. 프로젝트에는 설치되지 않는다.
- 스크립트가 하는 일은 파일 위쪽 설명에 적혀 있다. 요약하면 다음과 같다.
  1. 컬럼과 테이블의 설명(note)을 뺀 보기용 복사본을 만든다. 설명이 많으면 그림이 너무 커지기 때문이다.
  2. 컬럼 설명의 "식별자 참조: ... 테이블.컬럼" 내용을 읽어 **컨텍스트 사이 관계선**을 그림에 더한다. 이 선은 이해를 돕는 용도이고, 실제 스키마에는 외래키가 없다([ADR-0007](../adr/0007-context-deployment-strategy.md)).
  3. 렌더러로 SVG를 만들고 엣지로 PNG를 캡처한다.
- 옵션: `-Dbml`(입력 파일), `-OutDir`(출력 폴더), `-Name`(출력 이름), `-Edge`(엣지 경로), `-Width`(PNG 가로 픽셀).
- 컬럼에 컨텍스트 사이 참조를 새로 추가할 때는 컬럼 설명을 `식별자 참조: 계정 컨텍스트의 seller_profile.id (외래키 아님)` 형식으로 쓴다. 이 형식을 따라야 관계선이 자동으로 그려진다.

- 렌더러가 읽지 못하는 검사 제약(`checks`) 블록은 보기용 복사본에서 빼므로 그림에는 나오지 않는다.

## 관계의 선택/필수를 표시한 ERD 만들기

위의 `schema-erd.png`는 DBML 렌더러가 그려서 관계가 `1`과 `*`로만 나오고 "없을 수 있는지(0..1)"는 표시하지 못한다. 선택/필수까지 보려면 정보공학 표기(까마귀발)로 그리는 이 ERD를 쓴다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File docs\diagrams\render-erd-ie.ps1 -PlantumlJar C:\tools\plantuml\plantuml-1.2026.8.jar
```

- 결과는 `schema-erd-ie.puml`(원본)과 `schema-erd-ie.png`이며 `specs/001-multiseller-commerce-core/diagrams/`에 만들어진다. `.puml`은 스크립트가 만드는 파일이므로 직접 고치지 않는다.
- 옵션: `-Dbml`, `-OutDir`, `-Name`, `-PlantumlJar`(기본값 `C:\tools\plantuml\plantuml-1.2026.8.jar`), `-GraphvizDot`.
- 변환은 `dbml-to-ie-puml.js`가 한다. 읽는 규칙은 다음과 같다.
  - 컬럼 앞의 점(●)은 필수(`NOT NULL`)이고, 없으면 `NULL`을 허용한다.
  - 부모 쪽 기호는 외래키 컬럼이 `NOT NULL`이면 `||`(정확히 하나), `NULL`을 허용하면 `|o`(없거나 하나)다.
  - 자식 쪽 기호는 외래키 컬럼에 유일 제약이 있으면 `o|`(없거나 하나), 없으면 `o{`(0개 이상)이다.
  - 실선은 외래키 관계, 점선은 컨텍스트 사이 식별자 참조(외래키 없음)다. 점선은 컬럼 설명의 `식별자 참조: … 테이블.컬럼` 형식에서 읽는다.
- "자식이 1개 이상이어야 한다" 같은 최소 1 규칙은 데이터베이스가 지키지 못하므로 이 그림에는 나오지 않는다. 그런 업무 규칙은 클래스 다이어그램(`data-model-class.png`)의 다중도에 있다.

## SQL(참조 DDL) 만들기

`schema.dbml`에서 PostgreSQL용 `CREATE TABLE` 문을 만든다. 저장소 루트에서 실행한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File docs\diagrams\generate-sql.ps1
```

- 결과는 `specs/001-multiseller-commerce-core/schema.sql`이다. 직접 고치지 않고 DBML을 고쳐 다시 만든다.
- 이 파일은 설계 단계의 참조용이며 **마이그레이션이 아니다.** 실제 스키마 변경은 구현 단계에서 플라이웨이 마이그레이션으로 한다(헌법 기술 스택 섹션).
- DBML의 `checks` 블록(음수 금지, 법인 정보 필수 여부 등)은 `CHECK` 제약으로 변환된다.
- DBML로 표현할 수 없는 제약(성공 결제는 주문당 하나인 부분 유일 인덱스)은 `generate-sql.ps1` 안의 `$extra`에 직접 적어 끝에 붙인다. 이런 제약을 늘릴 때는 그곳에 추가한다.
- 변환기(`@dbml/core`)는 임시 폴더에 설치했다가 지우므로 프로젝트에는 남지 않는다.
- 명령줄 도구(`@dbml/cli`)는 윈도우에서 경로 문제로 오류가 나서 쓰지 않고, 변환 라이브러리를 노드로 직접 호출한다.
- 만들어진 SQL을 실제 데이터베이스에 적용해 검증하는 일은 이 스크립트에 포함되지 않는다. 구현 단계의 마이그레이션 작성 때 포스트그레스큐엘에서 확인한다.

## 문제 해결

| 증상 | 확인할 것 |
|------|-----------|
| 한글이 네모로 나온다 | 명령에 `-SdefaultFontName="Malgun Gothic"`이 있는지, 그 글꼴이 설치되어 있는지 |
| 유즈케이스 그림이 만들어지지 않는다 | `GRAPHVIZ_DOT` 환경변수가 맞는지, `dot.exe`가 있는지 |
| ERD 스크립트에서 엣지를 찾지 못한다 | `-Edge` 옵션으로 `msedge.exe` 경로를 지정 |
| ERD 스크립트가 한글 부분에서 문법 오류를 낸다 | 스크립트 파일이 BOM이 있는 UTF-8인지(윈도우 파워셸 5.1은 BOM이 없으면 한글을 잘못 읽는다). 고칠 때는 BOM을 유지해서 저장한다. |
| SVG를 만들지 못했다고 나온다 | 네트워크와 npm 설치를 확인한다. 회사 환경이면 npm 레지스트리 접근 허용이 필요할 수 있다. |
