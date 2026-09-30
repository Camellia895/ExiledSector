$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$modTarget   = "E:\Dev\Starsector\mods\ExiledSector"

. (Join-Path $projectRoot "build-common.ps1")

Invoke-ModBuild -ProjectRoot $projectRoot

Copy-ModFiles -ProjectRoot $projectRoot -Destination $modTarget

Write-Host "Deployed ExiledSector to $modTarget"
