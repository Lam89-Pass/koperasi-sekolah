# Script to download required library JARs for the Java project
$libDir = Join-Path $PSScriptRoot "lib"

# Create lib folder if not exists
if (-not (Test-Path $libDir)) {
    New-Item -ItemType Directory -Force -Path $libDir | Out-Null
    Write-Host "Created folder: $libDir" -ForegroundColor Green
}

# Define downloads
$downloads = @(
    @{
        Url = "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
        OutFile = Join-Path $libDir "mysql-connector-j-8.4.0.jar"
        Name = "MySQL Connector J"
    },
    @{
        Url = "https://repo1.maven.org/maven2/com/formdev/flatlaf/3.5.1/flatlaf-3.5.1.jar"
        OutFile = Join-Path $libDir "flatlaf-3.5.1.jar"
        Name = "FlatLaf Look and Feel"
    },
    @{
        Url = "https://repo1.maven.org/maven2/com/zaxxer/HikariCP/4.0.3/HikariCP-4.0.3.jar"
        OutFile = Join-Path $libDir "HikariCP-4.0.3.jar"
        Name = "HikariCP Connection Pool"
    },
    @{
        Url = "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar"
        OutFile = Join-Path $libDir "slf4j-api-1.7.36.jar"
        Name = "SLF4J API"
    },
    @{
        Url = "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar"
        OutFile = Join-Path $libDir "slf4j-simple-1.7.36.jar"
        Name = "SLF4J Simple Logger"
    }
)

# Perform downloads
foreach ($item in $downloads) {
    if (-not (Test-Path $item.OutFile)) {
        Write-Host "Downloading $($item.Name) from $($item.Url)..." -ForegroundColor Yellow
        try {
            Invoke-WebRequest -Uri $item.Url -OutFile $item.OutFile -UseBasicParsing
            Write-Host "Downloaded successfully: $($item.OutFile)" -ForegroundColor Green
        } catch {
            Write-Error "Failed to download $($item.Name). Error: $_"
        }
    } else {
        Write-Host "$($item.Name) already exists at $($item.OutFile)" -ForegroundColor Cyan
    }
}

Write-Host "Library download process completed." -ForegroundColor Green
