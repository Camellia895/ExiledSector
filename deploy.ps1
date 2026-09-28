$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$modTarget   = "E:\Dev\Starsector\mods\ExiledSector"

. (Join-Path $projectRoot "build-common.ps1")

Invoke-ModBuild -ProjectRoot $projectRoot

New-Item -ItemType Directory -Force -Path $modTarget | Out-Null

Copy-Item -Path (Join-Path $projectRoot "mod_info.json") -Destination $modTarget -Force
Copy-Item -Path (Join-Path $projectRoot "ExiledSector.version") -Destination $modTarget -Force
Copy-Item -Path (Join-Path $projectRoot "jars") -Destination $modTarget -Recurse -Force

$dataDir = Join-Path $projectRoot "data"
if (Test-Path $dataDir) {
    Copy-Item -Path $dataDir -Destination $modTarget -Recurse -Force
}

$graphicsDir = Join-Path $projectRoot "graphics"
if (Test-Path $graphicsDir) {
    Copy-Item -Path $graphicsDir -Destination $modTarget -Recurse -Force
}

Write-Host "Deployed ExiledSector to $modTarget"
