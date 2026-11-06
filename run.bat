@echo off
title Movie Ticket Booking System
setlocal

if "%JAVA_HOME%"=="" (
    if exist "C:\Users\vaisa\.jdks\jdk-17" (
        set "JAVA_HOME=C:\Users\vaisa\.jdks\jdk-17"
    )
)
if not "%JAVA_HOME%"=="" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

echo ==========================================================
echo Starting Movie Ticket Booking System (Spring Boot)
echo ==========================================================
echo Java: %JAVA_HOME%
echo.

call .\mvnw.cmd spring-boot:run
pause
