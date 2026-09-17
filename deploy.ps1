$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$modTarget   = "E:\Dev\Starsector\mods\ExiledSector"

$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnCmd) {
    $mvn = $mvnCmd.Source
} else {
    $mvn = Get-ChildItem "C:\Program Files\JetBrains\IntelliJ IDEA*\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" |
        Select-Object -First 1 -ExpandProperty FullName
    if (-not $mvn) {
        throw "Could not find mvn on PATH or bundled with IntelliJ IDEA."
    }
    if (-not $env:JAVA_HOME) {
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
    }
}

Push-Location $projectRoot
try {
    & $mvn -q clean package
    if ($LASTEXITCODE -ne 0) {
        throw "Maven build failed."
    }
} finally {
    Pop-Location
}

New-Item -ItemType Directory -Force -Path $modTarget | Out-Null

Copy-Item -Path (Join-Path $projectRoot "mod_info.json") -Destination $modTarget -Force
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
