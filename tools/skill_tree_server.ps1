$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$port = 8791
$toolsDir = $PSScriptRoot
$projectRoot = Split-Path -Parent $toolsDir
$editorPath = Join-Path $toolsDir "skill_tree_editor.html"
$typesPath = Join-Path $projectRoot "data\skilltrees\skill_types.json"
$treePath = Join-Path $projectRoot "data\skilltrees\ship_skill_tree.json"
$staticImagesDir = Join-Path $projectRoot "graphics\backgrounds\static_images"
$graphicsDir = Join-Path $projectRoot "graphics"
$ringTexturePaths = @{
    asteroids = Join-Path $projectRoot "graphics\planets\rings_asteroids0.png"
    ice       = Join-Path $projectRoot "graphics\planets\rings_ice0.png"
    dust      = Join-Path $projectRoot "graphics\planets\rings_dust0.png"
    special   = Join-Path $projectRoot "graphics\planets\rings_special0.png"
}

function HueToRgbChannel($p, $q, $t) {
    if ($t -lt 0) { $t += 1 }
    if ($t -gt 1) { $t -= 1 }
    if ($t -lt (1.0/6)) { return $p + ($q - $p) * 6 * $t }
    if ($t -lt 0.5) { return $q }
    if ($t -lt (2.0/3)) { return $p + ($q - $p) * (2.0/3 - $t) * 6 }
    return $p
}

function ColorFromAhsl($a, $h, $s, $l) {
    if ($s -le 0) {
        $v = [int]([math]::Round($l * 255))
        return [System.Drawing.Color]::FromArgb($a, $v, $v, $v)
    }
    $q = if ($l -lt 0.5) { $l * (1 + $s) } else { $l + $s - $l * $s }
    $p = 2 * $l - $q
    $hk = ((($h % 360) + 360) % 360) / 360.0
    $r = HueToRgbChannel $p $q ($hk + 1.0/3)
    $g = HueToRgbChannel $p $q $hk
    $b = HueToRgbChannel $p $q ($hk - 1.0/3)
    return [System.Drawing.Color]::FromArgb($a, [int]([math]::Round($r*255)), [int]([math]::Round($g*255)), [int]([math]::Round($b*255)))
}

function HueShiftImage($srcPath, $destPath, $hueShift) {
    $src = New-Object System.Drawing.Bitmap($srcPath)
    try {
        $out = New-Object System.Drawing.Bitmap($src.Width, $src.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        for ($y = 0; $y -lt $src.Height; $y++) {
            for ($x = 0; $x -lt $src.Width; $x++) {
                $c = $src.GetPixel($x, $y)
                if ($c.A -eq 0) {
                    $out.SetPixel($x, $y, $c)
                    continue
                }
                $h = $c.GetHue() + $hueShift
                $out.SetPixel($x, $y, (ColorFromAhsl $c.A $h $c.GetSaturation() $c.GetBrightness()))
            }
        }
        $out.Save($destPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $out.Dispose()
    } finally {
        $src.Dispose()
    }
}

function MakeCircularImage($srcPath, $destPath) {
    $src = New-Object System.Drawing.Bitmap($srcPath)
    try {
        $out = New-Object System.Drawing.Bitmap($src.Width, $src.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $g = [System.Drawing.Graphics]::FromImage($out)
        try {
            $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
            $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
            $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
            $g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
            $brush = New-Object System.Drawing.TextureBrush($src, [System.Drawing.Drawing2D.WrapMode]::Clamp)
            try {
                $g.FillEllipse($brush, 0, 0, $src.Width, $src.Height)
            } finally {
                $brush.Dispose()
            }
        } finally {
            $g.Dispose()
        }
        $out.Save($destPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $out.Dispose()
    } finally {
        $src.Dispose()
    }
}


# Vanilla planet-ring textures (rings_asteroids0/ice0/dust0/special0.png) are laid out as one or
# more 256px-wide vertical bands, each band fading from transparent to solid across its width
# (the radial cross-section of the ring) and tiling seamlessly along its height (the direction
# that wraps around the ring's circumference). This bakes one band into a flat annulus image by
# polar-remapping it: for every output pixel, its distance from center picks the radial sample
# (u, across the 256px band width) and its angle picks the tiled sample (v, wrapped around the
# band's height, repeated $tileCount times per revolution).
function GenerateRingBelt($srcPath, $destPath, $innerRadiusWorld, $outerRadiusWorld, $bandIndex, $tileCount, $canvasPixels) {
    $srcFull = New-Object System.Drawing.Bitmap($srcPath)
    try {
        $bandW = 256
        $bandCount = [Math]::Max(1, [int]($srcFull.Width / $bandW))
        $bi = (($bandIndex % $bandCount) + $bandCount) % $bandCount
        $bandH = $srcFull.Height

        $band = New-Object System.Drawing.Bitmap($bandW, $bandH, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $bg = [System.Drawing.Graphics]::FromImage($band)
        try {
            $srcRect = New-Object System.Drawing.Rectangle ($bi * $bandW), 0, $bandW, $bandH
            $dstRect = New-Object System.Drawing.Rectangle 0, 0, $bandW, $bandH
            $bg.DrawImage($srcFull, $dstRect, $srcRect, [System.Drawing.GraphicsUnit]::Pixel)
        } finally { $bg.Dispose() }

        $bandRect = New-Object System.Drawing.Rectangle 0, 0, $bandW, $bandH
        $bandData = $band.LockBits($bandRect, [System.Drawing.Imaging.ImageLockMode]::ReadOnly, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $bandStride = $bandData.Stride
        $bandBytes = New-Object byte[] ($bandStride * $bandH)
        [System.Runtime.InteropServices.Marshal]::Copy($bandData.Scan0, $bandBytes, 0, $bandBytes.Length)
        $band.UnlockBits($bandData)
        $band.Dispose()

        # Everything from here on works in a fixed-size pixel canvas independent of the anchor's
        # world-unit radius (the staticImage's width/height stretches it back up) - keeps
        # generation time bounded even for large rings.
        $scale = $canvasPixels / (2.0 * $outerRadiusWorld)
        $innerR = $innerRadiusWorld * $scale
        $outerR = $outerRadiusWorld * $scale
        $thickness = $outerR - $innerR
        $cx = $canvasPixels / 2.0
        $cy = $canvasPixels / 2.0

        $out = New-Object System.Drawing.Bitmap($canvasPixels, $canvasPixels, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $outRect = New-Object System.Drawing.Rectangle 0, 0, $canvasPixels, $canvasPixels
        $outData = $out.LockBits($outRect, [System.Drawing.Imaging.ImageLockMode]::WriteOnly, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $outStride = $outData.Stride
        $outBytes = New-Object byte[] ($outStride * $canvasPixels)
        $twoPi = 2.0 * [Math]::PI

        for ($y = 0; $y -lt $canvasPixels; $y++) {
            $dy = $y - $cy
            $rowOffset = $y * $outStride
            for ($x = 0; $x -lt $canvasPixels; $x++) {
                $dx = $x - $cx
                $r = [Math]::Sqrt($dx * $dx + $dy * $dy)
                if ($r -lt $innerR -or $r -gt $outerR) { continue }
                $theta = [Math]::Atan2($dy, $dx)
                if ($theta -lt 0) { $theta += $twoPi }
                $u = ($r - $innerR) / $thickness
                $v = ($theta / $twoPi) * $tileCount
                $v = $v - [Math]::Floor($v)

                $srcX = [int]($u * $bandW)
                if ($srcX -ge $bandW) { $srcX = $bandW - 1 }
                $srcY = [int]($v * $bandH)
                if ($srcY -ge $bandH) { $srcY = $bandH - 1 }

                $srcOffset = $srcY * $bandStride + $srcX * 4
                $dstOffset = $rowOffset + $x * 4
                $outBytes[$dstOffset]     = $bandBytes[$srcOffset]
                $outBytes[$dstOffset + 1] = $bandBytes[$srcOffset + 1]
                $outBytes[$dstOffset + 2] = $bandBytes[$srcOffset + 2]
                $outBytes[$dstOffset + 3] = $bandBytes[$srcOffset + 3]
            }
        }
        [System.Runtime.InteropServices.Marshal]::Copy($outBytes, 0, $outData.Scan0, $outBytes.Length)
        $out.UnlockBits($outData)
        $out.Save($destPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $out.Dispose()
    } finally {
        $srcFull.Dispose()
    }
}

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
            elseif ($request.HttpMethod -eq "GET" -and $request.Url.LocalPath -eq "/list-all-images") {
                $files = @()
                if (Test-Path $graphicsDir) {
                    $files = Get-ChildItem -Path $graphicsDir -Filter "*.png" -File -Recurse |
                        ForEach-Object {
                            $rel = $_.FullName.Substring($projectRoot.Length + 1) -replace '\\', '/'
                            $rel
                        } | Sort-Object
                }
                $json = ConvertTo-Json -InputObject @($files)
                $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
                $response.ContentType = "application/json; charset=utf-8"
                $response.OutputStream.Write($bytes, 0, $bytes.Length)
            }
            elseif ($request.HttpMethod -eq "POST" -and $request.Url.LocalPath -eq "/colorshift") {
                $reader = New-Object System.IO.StreamReader($request.InputStream, [System.Text.Encoding]::UTF8)
                $bodyText = $reader.ReadToEnd()
                $body = $bodyText | ConvertFrom-Json

                $relPath = [string]$body.path
                $suffix = ([string]$body.suffix).Trim()
                $hueShift = 0
                try { $hueShift = [double]$body.hueShift } catch { $hueShift = 0 }

                $srcFull = [System.IO.Path]::GetFullPath((Join-Path $projectRoot $relPath))
                $fullProjectRoot = [System.IO.Path]::GetFullPath($projectRoot)

                if (-not $relPath -or -not $srcFull.StartsWith($fullProjectRoot, [StringComparison]::OrdinalIgnoreCase) -or -not (Test-Path $srcFull -PathType Leaf)) {
                    Write-JsonResponse $response 400 @{ ok = $false; message = "Source image not found." }
                } elseif (-not $suffix -or $suffix -match '[\\/:]') {
                    Write-JsonResponse $response 400 @{ ok = $false; message = "Suffix is required and cannot contain path separators." }
                } else {
                    $dir = [System.IO.Path]::GetDirectoryName($srcFull)
                    $baseName = [System.IO.Path]::GetFileNameWithoutExtension($srcFull)
                    $destFull = Join-Path $dir ($baseName + "_" + $suffix + ".png")
                    try {
                        HueShiftImage $srcFull $destFull $hueShift
                        $destRel = $destFull.Substring($fullProjectRoot.Length + 1) -replace '\\', '/'
                        Write-JsonResponse $response 200 @{ ok = $true; path = $destRel }
                    } catch {
                        Write-JsonResponse $response 500 @{ ok = $false; message = $_.Exception.Message }
                    }
                }
            }
            elseif ($request.HttpMethod -eq "POST" -and $request.Url.LocalPath -eq "/make-circular") {
                $reader = New-Object System.IO.StreamReader($request.InputStream, [System.Text.Encoding]::UTF8)
                $bodyText = $reader.ReadToEnd()
                $body = $bodyText | ConvertFrom-Json

                $relPath = [string]$body.path
                $suffix = ([string]$body.suffix).Trim()

                $srcFull = [System.IO.Path]::GetFullPath((Join-Path $projectRoot $relPath))
                $fullProjectRoot = [System.IO.Path]::GetFullPath($projectRoot)

                if (-not $relPath -or -not $srcFull.StartsWith($fullProjectRoot, [StringComparison]::OrdinalIgnoreCase) -or -not (Test-Path $srcFull -PathType Leaf)) {
                    Write-JsonResponse $response 400 @{ ok = $false; message = "Source image not found." }
                } elseif (-not $suffix -or $suffix -match '[\\/:]') {
                    Write-JsonResponse $response 400 @{ ok = $false; message = "Suffix is required and cannot contain path separators." }
                } else {
                    $dir = [System.IO.Path]::GetDirectoryName($srcFull)
                    $baseName = [System.IO.Path]::GetFileNameWithoutExtension($srcFull)
                    $destFull = Join-Path $dir ($baseName + "_" + $suffix + ".png")
                    try {
                        MakeCircularImage $srcFull $destFull
                        $destRel = $destFull.Substring($fullProjectRoot.Length + 1) -replace '\\', '/'
                        Write-JsonResponse $response 200 @{ ok = $true; path = $destRel }
                    } catch {
                        Write-JsonResponse $response 500 @{ ok = $false; message = $_.Exception.Message }
                    }
                }
            }
            elseif ($request.HttpMethod -eq "POST" -and $request.Url.LocalPath -eq "/generate-ring-belt") {
                $reader = New-Object System.IO.StreamReader($request.InputStream, [System.Text.Encoding]::UTF8)
                $bodyText = $reader.ReadToEnd()
                $body = $bodyText | ConvertFrom-Json

                $anchorId = [string]$body.anchorId
                $style = [string]$body.style
                $innerRadius = 0.0
                $outerRadius = 0.0
                try { $innerRadius = [double]$body.innerRadius } catch { $innerRadius = 0.0 }
                try { $outerRadius = [double]$body.outerRadius } catch { $outerRadius = 0.0 }

                if (-not $anchorId -or $anchorId -notmatch '^[A-Za-z0-9_-]+$') {
                    Write-JsonResponse $response 400 @{ ok = $false; message = "Invalid anchor id." }
                } elseif (-not $ringTexturePaths.ContainsKey($style)) {
                    Write-JsonResponse $response 400 @{ ok = $false; message = "Unknown ring style: $style" }
                } elseif ($outerRadius -le $innerRadius -or $innerRadius -lt 0) {
                    Write-JsonResponse $response 400 @{ ok = $false; message = "Outer radius must be greater than inner radius." }
                } else {
                    $srcFull = $ringTexturePaths[$style]
                    if (-not (Test-Path $srcFull -PathType Leaf)) {
                        Write-JsonResponse $response 500 @{ ok = $false; message = "Ring texture not found: $srcFull" }
                    } else {
                        try {
                            $destFull = Join-Path $staticImagesDir ($anchorId + "_ringbelt.png")
                            $canvasPixels = 1200
                            $circumference = [Math]::PI * ($innerRadius + $outerRadius)
                            # Tile count derived from the source band's own aspect ratio (height/width,
                            # 512/256 = 2 for all our band textures) rather than an arbitrary
                            # world-units-per-tile constant, so each repeat stays roughly as long
                            # (along the ring) as the band is thick regardless of the anchor's actual
                            # size - matches the same reasoning used in RingBeltRenderer.java for the
                            # live in-game belts (see that file for why a fixed pixels-per-tile number
                            # doesn't work: it's calibrated for vanilla's campaign-scale planet rings,
                            # not an editor-placed anchor whose radius could be anything).
                            $thickness = $outerRadius - $innerRadius
                            $bandAspectRatio = 2.0
                            $tileDensity = 3.0
                            $tileCount = [Math]::Max(1, [int][Math]::Round($tileDensity * $circumference / ($thickness * $bandAspectRatio)))
                            $bandIndex = Get-Random -Minimum 0 -Maximum 4
                            GenerateRingBelt $srcFull $destFull $innerRadius $outerRadius $bandIndex $tileCount $canvasPixels
                            $fullProjectRoot = [System.IO.Path]::GetFullPath($projectRoot)
                            $destRel = $destFull.Substring($fullProjectRoot.Length + 1) -replace '\\', '/'
                            Write-JsonResponse $response 200 @{ ok = $true; path = $destRel; size = [int]([Math]::Ceiling($outerRadius * 2)) }
                        } catch {
                            Write-JsonResponse $response 500 @{ ok = $false; message = $_.Exception.Message }
                        }
                    }
                }
            }
            elseif ($request.HttpMethod -eq "POST" -and $request.Url.LocalPath -eq "/delete-image") {
                $reader = New-Object System.IO.StreamReader($request.InputStream, [System.Text.Encoding]::UTF8)
                $bodyText = $reader.ReadToEnd()
                $body = $bodyText | ConvertFrom-Json

                $relPath = [string]$body.path
                $srcFull = [System.IO.Path]::GetFullPath((Join-Path $projectRoot $relPath))
                $fullProjectRoot = [System.IO.Path]::GetFullPath($projectRoot)

                if (-not $relPath -or -not $srcFull.StartsWith($fullProjectRoot, [StringComparison]::OrdinalIgnoreCase) -or -not (Test-Path $srcFull -PathType Leaf)) {
                    Write-JsonResponse $response 400 @{ ok = $false; message = "Image not found." }
                } else {
                    try {
                        Remove-Item -Path $srcFull -Force
                        Write-JsonResponse $response 200 @{ ok = $true }
                    } catch {
                        Write-JsonResponse $response 500 @{ ok = $false; message = $_.Exception.Message }
                    }
                }
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
