# reset-db.ps1 — Drops and recreates the APIForge PostgreSQL database
# WARNING: destroys all data. Use only in dev.
# Usage: .\scripts\reset-db.ps1

param([switch]$Confirm)

if (-not $Confirm) {
    $answer = Read-Host "This will DELETE all PostgreSQL data. Type 'yes' to continue"
    if ($answer -ne 'yes') { Write-Host "Aborted."; exit 0 }
}

Write-Host "Resetting PostgreSQL database..." -ForegroundColor Yellow
docker exec apiforge-postgres psql -U apiforge -c "DROP DATABASE IF EXISTS apiforge_db;"
docker exec apiforge-postgres psql -U apiforge -c "CREATE DATABASE apiforge_db;"
Write-Host "Database reset. Flyway will re-run migrations on next backend start." -ForegroundColor Green
