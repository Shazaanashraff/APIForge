# run-smoke-tests.ps1 — Runs the Section Validation Gate smoke tests
# A section is NOT complete until these all pass.
# Usage: .\scripts\run-smoke-tests.ps1

Set-Location $PSScriptRoot\..

$passed = 0
$failed = 0

function Test-Http {
    param($Name, $Url, $Method = 'GET', $ExpectedStatus = 200)
    try {
        $response = Invoke-WebRequest -Uri $Url -Method $Method -TimeoutSec 10 -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -eq $ExpectedStatus) {
            Write-Host "  PASS $Name" -ForegroundColor Green
            $script:passed++
        } else {
            Write-Host "  FAIL $Name — expected $ExpectedStatus, got $($response.StatusCode)" -ForegroundColor Red
            $script:failed++
        }
    } catch {
        Write-Host "  FAIL $Name — $($_.Exception.Message)" -ForegroundColor Red
        $script:failed++
    }
}

function Test-Redis {
    $result = docker exec apiforge-redis redis-cli ping 2>$null
    if ($result -eq 'PONG') {
        Write-Host "  PASS Redis PING" -ForegroundColor Green
        $script:passed++
    } else {
        Write-Host "  FAIL Redis PING" -ForegroundColor Red
        $script:failed++
    }
}

function Test-Postgres {
    $result = docker exec apiforge-postgres pg_isready -U apiforge -d apiforge_db 2>$null
    if ($result -match 'accepting connections') {
        Write-Host "  PASS Postgres accepting connections" -ForegroundColor Green
        $script:passed++
    } else {
        Write-Host "  FAIL Postgres not ready" -ForegroundColor Red
        $script:failed++
    }
}

Write-Host "`n=== APIForge Smoke Tests ===" -ForegroundColor Cyan

Write-Host "`nInfrastructure..."
Test-Redis
Test-Postgres

Write-Host "`nAPI endpoints..."
Test-Http "Backend health"      "http://localhost:8081/actuator/health"
Test-Http "Backend OpenAPI"     "http://localhost:8081/v3/api-docs"
Test-Http "Java sample health"  "http://localhost:8090/actuator/health"
Test-Http "Node sample health"  "http://localhost:3000/health"

Write-Host "`nResults: $passed passed, $failed failed" -ForegroundColor $(if ($failed -eq 0) { 'Green' } else { 'Red' })

if ($failed -gt 0) {
    exit 1
}
