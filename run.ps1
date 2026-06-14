# Script to compile and run the school cooperative application
$binDir = Join-Path $PSScriptRoot "bin"
$libDir = Join-Path $PSScriptRoot "lib"

# 1. Create bin folder if not exists
if (-not (Test-Path $binDir)) {
    New-Item -ItemType Directory -Force -Path $binDir | Out-Null
}

# 2. Get all Java source files recursively
Write-Host "Mencari file source Java..." -ForegroundColor Cyan
$sources = Get-ChildItem -Path (Join-Path $PSScriptRoot "src") -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }

if ($sources.Count -eq 0) {
    Write-Error "Tidak menemukan file .java di dalam direktori src/"
    exit 1
}

Write-Host "Ditemukan $($sources.Count) file Java. Memulai kompilasi..." -ForegroundColor Yellow

# 3. Compile Java files
$libsClasspath = Join-Path $libDir "*"
try {
    javac -encoding UTF-8 -cp $libsClasspath -d $binDir $sources
    Write-Host "Kompilasi sukses! File class disimpan di folder bin/" -ForegroundColor Green
} catch {
    Write-Error "Gagal melakukan kompilasi file Java. Periksa error di atas."
    exit 1
}

# 4. Run Application
Write-Host "Menjalankan aplikasi Koperasi Sekolah..." -ForegroundColor Green
java -cp "$binDir;$libsClasspath" App
