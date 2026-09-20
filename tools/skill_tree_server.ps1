$ErrorActionPreference = "Stop"

$port = 8791
$toolsDir = $PSScriptRoot
$projectRoot = Split-Path -Parent $toolsDir
$editorPath = Join-Path $toolsDir "skill_tree_editor.html"
$typesPath = Join-Path $projectRoot "data\skilltrees\skill_types.json"
$treePath = Join-Path $projectRoot "data\skilltrees\ship_skill_tree.json"
$staticImagesDir = Join-Path $projectRoot "graphics\backgrounds\static_images"

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
            elseif ($request.HttpMethod -eq "GET" -and $request.Url.LocalPath -eq "/list-images") {
                $files = @()
                if (Test-Path $staticImagesDir) {
                    $files = Get-ChildItem -Path $staticImagesDir -Filter "*.png" -File |
                        Sort-Object Name |
                        ForEach-Object { "graphics/backgrounds/static_images/" + $_.Name }
                }
                $json = ConvertTo-Json -InputObject @($files)
                $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
                $response.ContentType = "application/json; charset=utf-8"
                $response.OutputStream.Write($bytes, 0, $bytes.Length)
            }
            elseif ($request.HttpMethod -eq "GET") {
                $relPath = [Uri]::UnescapeDataString($request.Url.LocalPath.TrimStart('/'))
                $filePath = Join-Path $projectRoot $relPath
                $fullFilePath = [System.IO.Path]::GetFullPath($filePath)
                $fullProjectRoot = [System.IO.Path]::GetFullPath($projectRoot)
                if ($fullFilePath.StartsWith($fullProjectRoot, [StringComparison]::OrdinalIgnoreCase) -and (Test-Path $fullFilePath -PathType Leaf)) {
                    $ext = [System.IO.Path]::GetExtension($fullFilePath).ToLowerInvariant()
                    $contentType = switch ($ext) {
                        ".png" { "image/png" }
                        ".jpg" { "image/jpeg" }
                        ".jpeg" { "image/jpeg" }
                        default { "application/octet-stream" }
                    }
                    $bytes = [System.IO.File]::ReadAllBytes($fullFilePath)
                    $response.ContentType = $contentType
                    $response.OutputStream.Write($bytes, 0, $bytes.Length)
                } else {
                    $response.StatusCode = 404
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
