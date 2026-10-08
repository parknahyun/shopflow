# 관계의 선택/필수(0..1, 1)를 표시한 ERD(정보공학 표기) 만드는 스크립트
# DBML에서 PlantUML 파일(.puml)을 만들고 PNG로 변환한다. 사용법은 README.md를 본다.
#
# 필요한 것: 노드(npm 포함), 자바, 플랜트유엠엘 실행 파일, 그래프비즈

param(
    [string]$Dbml = "specs/001-multiseller-commerce-core/diagrams/schema.dbml",
    [string]$OutDir = "specs/001-multiseller-commerce-core/diagrams",
    [string]$Name = "schema-erd-ie",
    [string]$PlantumlJar = "C:\tools\plantuml\plantuml-1.2026.8.jar",
    [string]$GraphvizDot = "C:\Program Files\Graphviz\bin\dot.exe"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $Dbml)) { throw "DBML 파일이 없습니다: $Dbml" }
if (-not (Test-Path $PlantumlJar)) { throw "플랜트유엠엘 실행 파일이 없습니다. -PlantumlJar 로 경로를 지정하세요: $PlantumlJar" }
if (-not (Test-Path $GraphvizDot)) { throw "그래프비즈를 찾을 수 없습니다. -GraphvizDot 로 경로를 지정하세요: $GraphvizDot" }
New-Item -ItemType Directory -Force $OutDir | Out-Null

$utf8 = New-Object System.Text.UTF8Encoding($false)
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$tmp = Join-Path ([IO.Path]::GetTempPath()) ("erd-ie-" + [Guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Force $tmp | Out-Null

try {
    # 변환기 설치 (임시 폴더에만 설치되고 프로젝트에는 남지 않는다)
    Push-Location $tmp
    try {
        & cmd /c "npm install --silent --no-audit --no-fund @dbml/core 2>&1" | Out-Null
    } finally { Pop-Location }
    if (-not (Test-Path (Join-Path $tmp "node_modules\@dbml\core"))) {
        throw "@dbml/core를 설치하지 못했습니다. npm 설치와 네트워크를 확인하세요."
    }

    Copy-Item (Join-Path $here "dbml-to-ie-puml.js") $tmp -Force
    $dbmlFull = (Resolve-Path $Dbml).Path
    $pumlTmp = Join-Path $tmp "$Name.puml"
    Push-Location $tmp
    try {
        & node dbml-to-ie-puml.js $dbmlFull $pumlTmp 2>&1
        if ($LASTEXITCODE -ne 0) { throw "DBML을 PlantUML로 바꾸지 못했습니다. DBML 문법을 확인하세요." }
    } finally { Pop-Location }

    # 원본 .puml은 저장소에 남기고, PNG는 한글 글꼴과 해상도를 지정해 만든다
    $outFull = [IO.Path]::GetFullPath($OutDir)
    Copy-Item $pumlTmp (Join-Path $outFull "$Name.puml") -Force
    $env:GRAPHVIZ_DOT = $GraphvizDot
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & java "-Dfile.encoding=UTF-8" -jar $PlantumlJar -charset UTF-8 "-SdefaultFontName=Malgun Gothic" "-Sdpi=130" -tpng -o $outFull (Join-Path $outFull "$Name.puml") 2>&1 | Out-Null
    $ErrorActionPreference = $prev
    if (-not (Test-Path (Join-Path $outFull "$Name.png"))) { throw "PNG를 만들지 못했습니다." }
    Write-Host "완료: $OutDir\$Name.puml, $OutDir\$Name.png"
}
finally {
    Remove-Item -Recurse -Force $tmp -ErrorAction SilentlyContinue
}
