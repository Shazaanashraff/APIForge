# verify-system.ps1 — Checks that all services are healthy and reachable
# Usage: .\scripts\verify-system.ps1
# Run this at the start of every development session.

Set-Location $PSScriptRoot\..

$allPassed = $true

function Check-Url {
    param($Name, $Url, $ExpectedContent)
    try {
        $response = Invoke-WebRequest -Uri $Url -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        if ($ExpectedContent -and $response.Content -notmatch $ExpectedContent) {
            Write-Host "  FAIL $Name — unexpected response at $Url" -ForegroundColor Red
            $script:allPassed = $false
        } else {
            Write-Host "  OK   $Name ($Url)" -ForegroundColor Green
        }
    } catch {
        Write-Host "  FAIL $Name — $Url unreachable: $($_.Exception.Message)" -ForegroundColor Red
        $script:allPassed = $false
    }
}

function Check-DockerService {
    param($ContainerName)
    $state = docker inspect --format='{{.State.Health.Status}}' $ContainerName 2>$null
    if ($state -eq 'healthy') {
        Write-Host "  OK   Docker: $ContainerName (healthy)" -ForegroundColor Green
    } else {
        Write-Host "  FAIL Docker: $ContainerName (status: $state)" -ForegroundColor Red
        $script:allPassed = $false
    }
}

Write-Host "`n=== APIForge System Verification ===" -ForegroundColor Cyan
Write-Host "Checking Docker services..."
Check-DockerService "apiforge-postgres"
Check-DockerService "apiforge-redis"
Check-DockerService "apiforge-mongodb"

Write-Host "`nChecking HTTP endpoints..."
Check-Url "APIForge Backend"     "http://localhost:8081/actuator/health" "UP"
Check-Url "Java Sample API"      "http://localhost:8090/actuator/health" "UP"
Check-Url "Node Sample API"      "http://localhost:3000/health"          "ok"

Write-Host "`nChecking observability (full stack only)..."
Check-Url "Prometheus"   "http://localhost:9090/-/healthy" ""
Check-Url "Grafana"      "http://localhost:3001/api/health" ""
Check-Url "Loki"         "http://localhost:3100/ready" "ready"

if ($allPassed) {
    Write-Host "`nAll checks passed." -ForegroundColor Green
} else {
    Write-Host "`nSome checks failed. See RUNBOOK.md for troubleshooting." -ForegroundColor Red
    exit 1
}
