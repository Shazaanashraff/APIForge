# start-all.ps1 — Starts the full APIForge Docker Compose stack
# Usage: .\scripts\start-all.ps1

Set-Location $PSScriptRoot\..

Write-Host "Starting full APIForge stack..." -ForegroundColor Cyan
docker-compose up -d

Write-Host "`nWaiting for services to become healthy (up to 120s)..." -ForegroundColor Yellow
$timeout = 120
$elapsed = 0
do {
    Start-Sleep -Seconds 5
    $elapsed += 5
    $unhealthy = docker-compose ps --format json 2>$null | ConvertFrom-Json | Where-Object { $_.Health -ne 'healthy' -and $_.Health -ne '' }
    if ($unhealthy.Count -eq 0) { break }
    Write-Host "  Still waiting... ($elapsed/$timeout s)" -ForegroundColor DarkYellow
} while ($elapsed -lt $timeout)

Write-Host "`nService status:" -ForegroundColor Green
docker-compose ps

Write-Host "`nRun .\scripts\verify-system.ps1 to confirm all health checks pass." -ForegroundColor Cyan
