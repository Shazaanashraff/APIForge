# stop-all.ps1 — Stops the full Docker Compose stack (preserves volumes)
# Usage: .\scripts\stop-all.ps1
# To also wipe volumes: .\scripts\stop-all.ps1 -WipeVolumes

param([switch]$WipeVolumes)

Set-Location $PSScriptRoot\..

if ($WipeVolumes) {
    Write-Host "Stopping stack and DELETING all volumes..." -ForegroundColor Red
    docker-compose down -v
} else {
    Write-Host "Stopping stack (volumes preserved)..." -ForegroundColor Yellow
    docker-compose down
}

Write-Host "Done." -ForegroundColor Green
