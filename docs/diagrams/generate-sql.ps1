# DBML에서 PostgreSQL 참조 DDL(SQL) 만드는 스크립트
# 사용법은 README.md를 본다.
#
# 만들어지는 파일은 설계 단계의 참조용이며 마이그레이션이 아니다.
# 실제 스키마 변경은 구현 단계에서 플라이웨이 마이그레이션으로 한다(헌법 기술 스택 섹션).
#
# DBML이 표현하지 못하는 제약(부분 유일 인덱스)은 아래 $extra에 직접 적어 끝에 붙인다.
#
# 필요한 것: 노드(npm 포함)

param(
    [string]$Dbml = "specs/001-multiseller-commerce-core/diagrams/schema.dbml",
    [string]$Out = "specs/001-multiseller-commerce-core/schema.sql"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $Dbml)) { throw "DBML 파일이 없습니다: $Dbml" }

$utf8 = New-Object System.Text.UTF8Encoding($false)
$tmp = Join-Path ([IO.Path]::GetTempPath()) ("dbml-sql-" + [Guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Force $tmp | Out-Null

$extra = @'

-- ───────── DBML로 표현할 수 없는 제약 ─────────

-- 한 주문에 성공(succeeded) 결제는 하나만 있어야 한다.
CREATE UNIQUE INDEX "uq_payment_one_success_per_order"
  ON "payment" ("order_id")
  WHERE "status" = 'succeeded';
'@

try {
    # 변환기 설치 (임시 폴더에만 설치되고 프로젝트에는 남지 않는다)
    Push-Location $tmp
    try {
        & cmd /c "npm install --silent --no-audit --no-fund @dbml/core 2>&1" | Out-Null
    } finally { Pop-Location }
    if (-not (Test-Path (Join-Path $tmp "node_modules\@dbml\core"))) {
        throw "@dbml/core를 설치하지 못했습니다. npm 설치와 네트워크를 확인하세요."
    }

    $dbmlFull = (Resolve-Path $Dbml).Path
    $sqlTmp = Join-Path $tmp "out.sql"
    $js = @"
const fs = require('fs');
const { exporter } = require('@dbml/core');
const src = fs.readFileSync(process.argv[2], 'utf8');
fs.writeFileSync(process.argv[3], exporter.export(src, 'postgres'));
"@
    [IO.File]::WriteAllText((Join-Path $tmp "run.js"), $js, $utf8)
    Push-Location $tmp
    try {
        & node run.js $dbmlFull $sqlTmp 2>&1
        if ($LASTEXITCODE -ne 0) { throw "DBML을 SQL로 바꾸지 못했습니다. DBML 문법을 확인하세요." }
    } finally { Pop-Location }

    $header = @"
-- 샵플로우 핵심 구매·판매 흐름 스키마 (PostgreSQL 참조 DDL)
-- 이 파일은 diagrams/schema.dbml에서 docs/diagrams/generate-sql.ps1로 만든다. 직접 고치지 않는다.
-- 설계 단계의 참조용이며 마이그레이션이 아니다. 실제 변경은 플라이웨이 마이그레이션으로 한다.
-- 컨텍스트 사이 참조는 식별자 값만 저장하며 외래키가 없다(ADR-0007).

"@
    $body = [IO.File]::ReadAllText($sqlTmp, [Text.Encoding]::UTF8)
    New-Item -ItemType Directory -Force (Split-Path -Parent ([IO.Path]::GetFullPath($Out))) | Out-Null
    [IO.File]::WriteAllText([IO.Path]::GetFullPath($Out), $header + $body + $extra + "`n", $utf8)
    Write-Host "완료: $Out"
}
finally {
    Remove-Item -Recurse -Force $tmp -ErrorAction SilentlyContinue
}
