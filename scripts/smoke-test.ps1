# smoke-test.ps1 — End-to-end smoke test for APIForge
# Verifies the full pipeline is alive: health checks, spec parsing, test run execution.
#
# Usage:
#   .\scripts\smoke-test.ps1
#   .\scripts\smoke-test.ps1 -ApiForgeUrl http://localhost:8080 -NodeApiUrl http://localhost:3000
#
# Prerequisites:
#   - APIForge backend running (.\mvnw.cmd spring-boot:run in backend/)
#   - Sample Node API running (npm run dev in sample-target-api-node/)
#   - curl available on PATH

param(
    [string]$ApiForgeUrl = "http://localhost:8080",
    [string]$NodeApiUrl  = "http://localhost:3000",
    [string]$JavaApiUrl  = "http://localhost:8090"
)

$ErrorActionPreference = "Stop"
$pass = 0
$fail = 0

function Check {
    param([string]$Name, [scriptblock]$Block)
    try {
        & $Block
        Write-Host "  [PASS] $Name" -ForegroundColor Green
        $script:pass++
    } catch {
        Write-Host "  [FAIL] $Name — $($_.Exception.Message)" -ForegroundColor Red
        $script:fail++
    }
}

function Invoke-Api {
    param(
        [string]$Method = "GET",
        [string]$Url,
        [string]$Body = $null,
        [string]$ContentType = "application/json",
        [int]$ExpectedStatus = 200
    )
    $args = @("-s", "-o", "/dev/null", "-w", "%{http_code}", "-X", $Method, $Url)
    if ($Body) {
        $args += @("-H", "Content-Type: $ContentType", "-d", $Body)
    }
    $status = & curl @args
    if ([int]$status -ne $ExpectedStatus) {
        throw "Expected HTTP $ExpectedStatus but got $status"
    }
    return $status
}

function Invoke-ApiJson {
    param(
        [string]$Method = "GET",
        [string]$Url,
        [string]$Body = $null,
        [string]$ContentType = "application/json"
    )
    $curlArgs = @("-s", "-X", $Method, $Url)
    if ($Body) {
        $curlArgs += @("-H", "Content-Type: $ContentType", "-d", $Body)
    }
    $raw = & curl @curlArgs
    return $raw | ConvertFrom-Json
}

Write-Host ""
Write-Host "=== APIForge Smoke Test ===" -ForegroundColor Cyan
Write-Host "  APIForge : $ApiForgeUrl"
Write-Host "  Node API : $NodeApiUrl"
Write-Host "  Java API : $JavaApiUrl"
Write-Host ""

# ── 1. Health checks ──────────────────────────────────────────────────────────
Write-Host "1. Health checks" -ForegroundColor Yellow

Check "APIForge /actuator/health returns UP" {
    $json = Invoke-ApiJson -Url "$ApiForgeUrl/actuator/health"
    if ($json.status -ne "UP") { throw "status=$($json.status)" }
}

Check "Node API /health returns ok" {
    $json = Invoke-ApiJson -Url "$NodeApiUrl/health"
    if ($json.status -ne "ok") { throw "status=$($json.status)" }
}

# Java API health is optional — skip if not running
try {
    $jh = Invoke-ApiJson -Url "$JavaApiUrl/actuator/health" 2>$null
    if ($jh.status -eq "UP") {
        Check "Java API /actuator/health returns UP" { }
    }
} catch { Write-Host "  [SKIP] Java API not running" -ForegroundColor DarkGray }

# ── 2. Spec parsing ───────────────────────────────────────────────────────────
Write-Host ""
Write-Host "2. Spec parsing" -ForegroundColor Yellow

Check "Introspect Node API spec" {
    $json = Invoke-ApiJson -Method "POST" `
        -Url "$ApiForgeUrl/api/specs/introspect?baseUrl=$([Uri]::EscapeDataString($NodeApiUrl))"
    if (-not $json.endpointCount -or $json.endpointCount -lt 1) {
        throw "endpointCount=$($json.endpointCount)"
    }
    Write-Host "     endpoints found: $($json.endpointCount)" -ForegroundColor DarkGray
}

# ── 3. Full test run ──────────────────────────────────────────────────────────
Write-Host ""
Write-Host "3. Full test run against Node buggy API" -ForegroundColor Yellow

Check "POST /api/runs returns results" {
    $body = @{
        specUrl   = "$NodeApiUrl/api-docs/json"
        baseUrl   = $NodeApiUrl
        projectId = "smoke-test"
        tenantId  = "smoke-tenant"
    } | ConvertTo-Json -Compress

    $json = Invoke-ApiJson -Method "POST" -Url "$ApiForgeUrl/api/runs" -Body $body
    if (-not $json.testRunId) { throw "Missing testRunId in response" }
    if ($null -eq $json.results) { throw "Missing results array" }
    Write-Host "     runId  : $($json.testRunId)" -ForegroundColor DarkGray
    Write-Host "     passed : $($json.passed)  failed : $($json.failed)" -ForegroundColor DarkGray
}

Check "Bugs are detected (failed > 0)" {
    $body = @{
        specUrl   = "$NodeApiUrl/api-docs/json"
        baseUrl   = $NodeApiUrl
        projectId = "smoke-test"
        tenantId  = "smoke-tenant"
    } | ConvertTo-Json -Compress

    $json = Invoke-ApiJson -Method "POST" -Url "$ApiForgeUrl/api/runs" -Body $body
    if ($json.failed -lt 1) {
        throw "Expected at least 1 failed test — got $($json.failed). Are the sample bugs present?"
    }
}

# ── 4. Code generation ────────────────────────────────────────────────────────
Write-Host ""
Write-Host "4. Code generation" -ForegroundColor Yellow

Check "Generate RestAssured tests" {
    $body = @{
        specUrl     = "$NodeApiUrl/api-docs/json"
        format      = "REST_ASSURED"
        baseUrl     = $NodeApiUrl
        packageName = "com.example.smoke"
    } | ConvertTo-Json -Compress

    $json = Invoke-ApiJson -Method "POST" -Url "$ApiForgeUrl/api/code/generate" -Body $body
    if (-not $json.files -or $json.files.Count -lt 1) {
        throw "No generated files returned"
    }
    Write-Host "     files generated: $($json.files.Count)" -ForegroundColor DarkGray
}

Check "Generate K6 load test script" {
    $body = @{
        specUrl = "$NodeApiUrl/api-docs/json"
        format  = "K6"
        baseUrl = $NodeApiUrl
    } | ConvertTo-Json -Compress

    $json = Invoke-ApiJson -Method "POST" -Url "$ApiForgeUrl/api/code/generate" -Body $body
    if (-not $json.files -or $json.files.Count -lt 1) {
        throw "No K6 files returned"
    }
}

# ── Summary ───────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "=== Results ===" -ForegroundColor Cyan
Write-Host "  Passed : $pass" -ForegroundColor Green
if ($fail -gt 0) {
    Write-Host "  Failed : $fail" -ForegroundColor Red
    Write-Host ""
    Write-Host "Smoke test FAILED. Check the service logs and retry." -ForegroundColor Red
    exit 1
} else {
    Write-Host "  Failed : $fail"
    Write-Host ""
    Write-Host "Smoke test PASSED." -ForegroundColor Green
    exit 0
}
