# seed-keycloak.ps1 — Re-imports the APIForge realm into Keycloak
# Run this if the realm is missing or corrupted.
# Usage: .\scripts\seed-keycloak.ps1

Set-Location $PSScriptRoot\..

$realmFile = "observability/keycloak/realm-export.json"
if (-not (Test-Path $realmFile)) {
    Write-Host "ERROR: $realmFile not found. Create it first (see S03 plan)." -ForegroundColor Red
    exit 1
}

Write-Host "Re-importing Keycloak realm from $realmFile..." -ForegroundColor Cyan

# Delete existing realm if present
docker exec apiforge-keycloak /opt/keycloak/bin/kcadm.sh delete realms/apiforge `
    --no-config --server http://localhost:8080 `
    --realm master --user admin --password admin 2>$null

# Import realm
docker exec apiforge-keycloak /opt/keycloak/bin/kc.sh import `
    --file /opt/keycloak/data/import/realm-export.json

Write-Host "Realm imported. Verify at http://localhost:8080" -ForegroundColor Green
