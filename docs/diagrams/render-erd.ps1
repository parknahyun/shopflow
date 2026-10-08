# 스키마 ERD 이미지 생성 스크립트
# DBML 파일에서 SVG와 PNG를 만든다. 사용법은 README.md를 본다.
#
# 하는 일
#   1. DBML을 읽어 보기용 복사본을 임시 폴더에 만든다.
#      - 컬럼과 테이블 설명(note)을 뺀다. 설명이 많으면 그림이 너무 커진다.
#      - 컬럼 설명의 "식별자 참조: ... 의 테이블.컬럼" 내용을 읽어 컨텍스트 사이 관계선을 더한다.
#        이 선은 그림에만 그려지고 실제 스키마에는 외래키가 없다(ADR-0007).
#   2. DBML 렌더러로 SVG를 만든다.
#   3. Edge 헤드리스 모드로 SVG를 PNG로 캡처한다.
#
# 필요한 것: 노드(npm 포함), Microsoft Edge

param(
    [string]$Dbml = "specs/001-multiseller-commerce-core/diagrams/schema.dbml",
    [string]$OutDir = "specs/001-multiseller-commerce-core/diagrams",
    [string]$Name = "schema-erd",
    [string]$Edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
    [int]$Width = 3200
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $Dbml)) { throw "DBML 파일이 없습니다: $Dbml" }
if (-not (Test-Path $Edge)) { throw "Edge를 찾을 수 없습니다. -Edge 로 경로를 지정하세요: $Edge" }
New-Item -ItemType Directory -Force $OutDir | Out-Null

$utf8 = New-Object System.Text.UTF8Encoding($false)
$tmp = Join-Path ([IO.Path]::GetTempPath()) ("erd-" + [Guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Force $tmp | Out-Null

try {
    # 1. 보기용 복사본 만들기
    $src = [IO.File]::ReadAllText((Resolve-Path $Dbml), [Text.Encoding]::UTF8)

    # 컨텍스트 사이 식별자 참조를 읽는다 (Table 이름을 따라가며 note의 "식별자 참조" 줄을 찾는다)
    $refs = New-Object System.Collections.Generic.List[string]
    $table = $null
    foreach ($line in ($src -split "`r?`n")) {
        if ($line -match '^\s*Table\s+(\w+)\s*\{') { $table = $Matches[1]; continue }
        if ($line -match '^\s*(\w+)\s+[\w\(\),]+.*note:\s*''식별자 참조[^'']*?(\w+)\.(\w+)\s') {
            if ($table) { $refs.Add("Ref: $table.$($Matches[1]) > $($Matches[2]).$($Matches[3])") }
        }
    }

    # 설명과 이름 표시를 뺀다
    $view = [regex]::Replace($src, ",\s*note:\s*'[^']*'", "")
    $view = [regex]::Replace($view, "\[note:\s*'[^']*'\]", "")
    $view = [regex]::Replace($view, "(?m)^\s*Note:\s*'[^']*'\s*$", "")
    $view = [regex]::Replace($view, ",\s*name:\s*'[^']*'", "")
    $view = [regex]::Replace($view, "\[name:\s*'[^']*'\]", "")
    $view = [regex]::Replace($view, " \[\s*\]", "")
    # 렌더러가 읽지 못하는 검사 제약(checks) 블록을 뺀다
    $view = [regex]::Replace($view, "(?ms)^[ \t]*checks[ \t]*\{.*?^[ \t]*\}[ \t]*\r?\n", "")
    $view = $view + "`n// 보기 전용: 컨텍스트 사이 식별자 참조`n" + ($refs -join "`n") + "`n"
    $viewPath = Join-Path $tmp "view.dbml"
    [IO.File]::WriteAllText($viewPath, $view, $utf8)
    Write-Host "컨텍스트 사이 관계선 $($refs.Count)개를 더했습니다."

    # 2. SVG 만들기
    $svgPath = Join-Path $tmp "view.svg"
    Push-Location $tmp
    try {
        $renderOut = & cmd /c "npm exec --yes --package=@softwaretechnik/dbml-renderer -- dbml-renderer -i view.dbml -o view.svg 2>&1"
    } finally { Pop-Location }
    if (-not (Test-Path $svgPath)) {
        $renderOut | Select-Object -First 10 | ForEach-Object { Write-Host $_.ToString().Substring(0, [Math]::Min(200, $_.ToString().Length)) }
        throw "SVG를 만들지 못했습니다. 위 출력과 npm 설치, 네트워크를 확인하세요."
    }

    # 3. PNG로 캡처하기 (SVG 크기에 맞춰 창 크기를 정한다)
    $svg = [IO.File]::ReadAllText($svgPath, [Text.Encoding]::UTF8)
    $m = [regex]::Match($svg, '<svg width="([\d.]+)pt" height="([\d.]+)pt"')
    if (-not $m.Success) { throw "SVG 크기를 읽지 못했습니다." }
    $height = [int]($Width * [double]$m.Groups[2].Value / [double]$m.Groups[1].Value) + 20
    $html = "<html><body style='margin:0;background:#fff'><img src='view.svg' style='width:${Width}px;display:block'></body></html>"
    [IO.File]::WriteAllText((Join-Path $tmp "view.html"), $html, $utf8)
    $pngPath = Join-Path $tmp "view.png"
    $url = "file:///" + ($tmp -replace '\\', '/') + "/view.html"
    # Edge가 오류 출력을 내도(무해한 경고) 중단되지 않도록 잠시 오류 처리를 늦춘다
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & $Edge --headless=new --disable-gpu --hide-scrollbars "--window-size=$Width,$height" "--screenshot=$pngPath" $url 2>&1 | Out-Null
    $ErrorActionPreference = $prev
    $waited = 0
    while (-not (Test-Path $pngPath) -and $waited -lt 30) { Start-Sleep -Seconds 1; $waited++ }
    if (-not (Test-Path $pngPath)) { throw "PNG를 만들지 못했습니다." }

    Copy-Item $svgPath (Join-Path $OutDir "$Name.svg") -Force
    Copy-Item $pngPath (Join-Path $OutDir "$Name.png") -Force
    Write-Host "완료: $OutDir\$Name.svg, $OutDir\$Name.png"
}
finally {
    Remove-Item -Recurse -Force $tmp -ErrorAction SilentlyContinue
}
