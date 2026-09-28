function Sync-OpSpentHullModPool {
    param([string]$ProjectRoot)

    $javaFile = Join-Path $ProjectRoot "src\main\java\exiledsector\persistence\OpSpentSlotManager.java"
    $csvFile = Join-Path $ProjectRoot "data\hullmods\hull_mods.csv"

    $match = Select-String -Path $javaFile -Pattern 'SLOT_COUNT\s*=\s*(\d+)' | Select-Object -First 1
    if (-not $match) {
        throw "Could not find SLOT_COUNT in $javaFile"
    }
    $slotCount = [int]$match.Matches[0].Groups[1].Value

    $lines = Get-Content $csvFile -Encoding UTF8
    $header = $lines[0]
    $fixedRows = $lines[1..($lines.Count - 1)] | Where-Object { $_ -notmatch ',exiledSector_opSpent_\d+,' }
    $pool = 0..($slotCount - 1) | ForEach-Object {
        "Exiled Sector OP Reserve $_,exiledSector_opSpent_$_,,,,hide_in_codex,,,,TRUE,TRUE,0,0,0,0,exiledsector.effects.SkillTreeOpSpentHullMod,`"Internal marker hullmod used by Exiled Sector to reserve ordnance points spent on allocated skill tree nodes for one ship slot. Its OP cost is set programmatically. Always installed automatically; not player-visible.`",,,"
    }
    $out = @($header) + $fixedRows + $pool
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllLines($csvFile, $out, $utf8NoBom)
    Write-Host "Synced OP-reservation hullmod pool to $slotCount slots (from SLOT_COUNT in OpSpentSlotManager.java)"
}

function Get-MavenCommand {
    $mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mvnCmd) {
        return $mvnCmd.Source
    }
    $mvn = Get-ChildItem "C:\Program Files\JetBrains\IntelliJ IDEA*\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" |
        Select-Object -First 1 -ExpandProperty FullName
    if (-not $mvn) {
        throw "Could not find mvn on PATH or bundled with IntelliJ IDEA."
    }
    if (-not $env:JAVA_HOME) {
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
    }
    return $mvn
}

function Invoke-ModBuild {
    param([string]$ProjectRoot)

    Sync-OpSpentHullModPool -ProjectRoot $ProjectRoot
    $mvn = Get-MavenCommand
    Push-Location $ProjectRoot
    try {
        & $mvn -q clean package
        if ($LASTEXITCODE -ne 0) {
            throw "Maven build failed."
        }
    } finally {
        Pop-Location
    }
}
