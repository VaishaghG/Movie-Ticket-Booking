# One-click startup script for PowerShell
if (-not $env:JAVA_HOME) {
    if (Test-Path "C:\Users\vaisa\.jdks\jdk-17") {
        $env:JAVA_HOME = "C:\Users\vaisa\.jdks\jdk-17"
    }
}
if ($env:JAVA_HOME) {
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Starting Movie Ticket Booking System (Spring Boot)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Using Java: $env:JAVA_HOME" -ForegroundColor Green
Write-Host "Server will start on: http://localhost:8080" -ForegroundColor Yellow
Write-Host ""

& .\mvnw.cmd spring-boot:run
