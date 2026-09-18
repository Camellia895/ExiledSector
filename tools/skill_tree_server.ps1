$ErrorActionPreference = "Stop"

$port = 8791
$toolsDir = $PSScriptRoot
$projectRoot = Split-Path -Parent $toolsDir
$editorPath = Join-Path $toolsDir "skill_tree_editor.html"
$typesPath = Join-Path $projectRoot "data\skilltrees\skill_types.json"
$treePath = Join-Path $projectRoot "data\skilltrees\ship_skill_tree.json"

function Write-JsonResponse($response, $statusCode, $payload) {
    $response.StatusCode = $statusCode
    $response.ContentType = "application/json; charset=utf-8"
    $bytes = [System.Text.Encoding]::UTF8.GetBytes(($payload | ConvertTo-Json))
    $response.OutputStream.Write($bytes, 0, $bytes.Length)
}

$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add("http://localhost:$port/")
$listener.Start()
Write-Host "Skill tree editor running at http://localhost:$port/ - open that URL in your browser."
Write-Host "Saves write directly to:"
Write-Host "  $typesPath"
Write-Host "  $treePath"
Write-Host "Press Ctrl+C to stop."

try {
    while ($listener.IsListening) {
        $context = $listener.GetContext()
        $request = $context.Request
        $response = $context.Response
        try {
            if ($request.HttpMethod -eq "GET" -and ($request.Url.LocalPath -eq "/" -or $request.Url.LocalPath -eq "/index.html")) {
                $bytes = [System.IO.File]::ReadAllBytes($editorPath)
                $response.ContentType = "text/html; charset=utf-8"
                $response.OutputStream.Write($bytes, 0, $bytes.Length)
            }
            elseif ($request.HttpMethod -eq "GET" -and $request.Url.LocalPath -eq "/data/types") {
                $bytes = [System.IO.File]::ReadAllBytes($typesPath)
                $response.ContentType = "application/json; charset=utf-8"
                $response.OutputStream.Write($bytes, 0, $bytes.Length)
            }
            elseif ($request.HttpMethod -eq "GET" -and $request.Url.LocalPath -eq "/data/tree") {
                $bytes = [System.IO.File]::ReadAllBytes($treePath)
                $response.ContentType = "application/json; charset=utf-8"
                $response.OutputStream.Write($bytes, 0, $bytes.Length)
            }
            elseif ($request.HttpMethod -eq "POST" -and $request.Url.LocalPath -eq "/save") {
                $reader = New-Object System.IO.StreamReader($request.InputStream, [System.Text.Encoding]::UTF8)
                $bodyText = $reader.ReadToEnd()
                $body = $bodyText | ConvertFrom-Json

                $typesOk = $true
                $treeOk = $true
                try { $null = $body.types | ConvertFrom-Json } catch { $typesOk = $false }
                try { $null = $body.tree | ConvertFrom-Json } catch { $treeOk = $false }

                if (-not $typesOk -or -not $treeOk) {
                    $parts = @()
                    if (-not $typesOk) { $parts += "skill_types.json body did not parse as JSON" }
                    if (-not $treeOk) { $parts += "ship_skill_tree.json body did not parse as JSON" }
                    Write-JsonResponse $response 400 @{ ok = $false; message = ("Nothing was written - " + ($parts -join "; ") + ".") }
                } else {
                    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
                    [System.IO.File]::WriteAllText($typesPath, $body.types, $utf8NoBom)
                    [System.IO.File]::WriteAllText($treePath, $body.tree, $utf8NoBom)
                    Write-JsonResponse $response 200 @{ ok = $true; message = "Saved." }
                }
            }
            else {
                $response.StatusCode = 404
            }
        } catch {
            try { Write-JsonResponse $response 500 @{ ok = $false; message = $_.Exception.Message } } catch {}
        } finally {
            $response.OutputStream.Close()
        }
    }
} finally {
    $listener.Stop()
}
