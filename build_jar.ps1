# Script untuk melakukan kompilasi dan membuat single FAT JAR
$binDir = Join-Path $PSScriptRoot "bin"
$libDir = Join-Path $PSScriptRoot "lib"
$jarName = "KoperasiSekolah.jar"

# 1. Buat folder bin jika belum ada
if (-not (Test-Path $binDir)) {
    New-Item -ItemType Directory -Force -Path $binDir | Out-Null
}

$jarExe = Get-ChildItem -Path "C:\Program Files\Java", "C:\Program Files\Eclipse Adoptium" -Filter "jar.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName
if ([string]::IsNullOrWhiteSpace($jarExe)) { $jarExe = "jar" }

Write-Host "Mengekstrak library dari folder lib/..." -ForegroundColor Cyan
# Ekstrak semua isi dari file .jar di dalam lib/ ke dalam folder bin/
Get-ChildItem -Path $libDir -Filter "*.jar" | ForEach-Object {
    $libJar = $_.FullName
    Write-Host "Ekstrak: $libJar"
    Push-Location $binDir
    & $jarExe xf $libJar
    Pop-Location
}

# Hapus folder META-INF bawaan library agar tidak bentrok dengan manifest utama
$metaInfPath = Join-Path $binDir "META-INF"
if (Test-Path $metaInfPath) {
    Remove-Item -Path $metaInfPath -Recurse -Force
}

# Hapus module-info.class agar tidak error "package java.sql is not visible" di Java versi baru
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
    
    # Buat Manifest.mf baru (jika belum ada/perlu diperbarui)
    $manifestPath = Join-Path $PSScriptRoot "Manifest.mf"
    "Manifest-Version: 1.0`r`nMain-Class: App`r`n" | Out-File -FilePath $manifestPath -Encoding ASCII
    
    # Pindah ke direktori bin untuk membuat JAR
    Push-Location $binDir
    & $jarExe cvfm "../$jarName" "../Manifest.mf" . | Out-Null
    Pop-Location
    
    Write-Host "=============================================" -ForegroundColor Green
    Write-Host "BERHASIL! File $jarName versi terbaru sudah siap." -ForegroundColor Green
    Write-Host "=============================================" -ForegroundColor Green
} else {
    Write-Host "Gagal melakukan kompilasi." -ForegroundColor Red
}
