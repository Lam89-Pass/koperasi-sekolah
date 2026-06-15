$binDir = Join-Path $PSScriptRoot "bin"
$libDir = Join-Path $PSScriptRoot "lib"
$jarName = "KoperasiSekolah.jar"

if (-not (Test-Path $binDir)) {
    New-Item -ItemType Directory -Force -Path $binDir | Out-Null
}

$jarExe = Get-ChildItem -Path "C:\Program Files\Java", "C:\Program Files\Eclipse Adoptium" -Filter "jar.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName
if ([string]::IsNullOrWhiteSpace($jarExe)) { $jarExe = "jar" }

Write-Host "Mengekstrak library dari folder lib/..." -ForegroundColor Cyan
Get-ChildItem -Path $libDir -Filter "*.jar" | ForEach-Object {
    $libJar = $_.FullName
    Write-Host "Ekstrak: $libJar"
    Push-Location $binDir
    & $jarExe xf $libJar
    Pop-Location
}

$metaInfPath = Join-Path $binDir "META-INF"
if (Test-Path $metaInfPath) {
    Remove-Item -Path $metaInfPath -Recurse -Force
}

$moduleInfoPath = Join-Path $binDir "module-info.class"
if (Test-Path $moduleInfoPath) {
    Remove-Item -Path $moduleInfoPath -Force
}

Write-Host "Mencari file source Java..." -ForegroundColor Cyan
$sources = Get-ChildItem -Path (Join-Path $PSScriptRoot "src") -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }

Write-Host "Ditemukan $($sources.Count) file Java. Memulai kompilasi..." -ForegroundColor Yellow
$libsClasspath = Join-Path $libDir "*"
javac -encoding UTF-8 -cp $libsClasspath -d $binDir $sources

if ($LASTEXITCODE -eq 0) {
    Write-Host "Kompilasi sukses! Membuat file JAR..." -ForegroundColor Yellow
    
    $manifestPath = Join-Path $PSScriptRoot "Manifest.mf"
    "Manifest-Version: 1.0`r`nMain-Class: App`r`n" | Out-File -FilePath $manifestPath -Encoding ASCII
    
    Push-Location $binDir
    & $jarExe cvfm "../$jarName" "../Manifest.mf" . | Out-Null
    Pop-Location
    
    Write-Host "=============================================" -ForegroundColor Green
    Write-Host "BERHASIL! File $jarName versi terbaru sudah siap." -ForegroundColor Green
    Write-Host "=============================================" -ForegroundColor Green
} else {
    Write-Host "Gagal melakukan kompilasi." -ForegroundColor Red
}
