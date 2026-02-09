@echo off
REM Batch script to download Bouncy Castle libraries for Java 17
REM This script uses PowerShell to download the files

echo ========================================
echo Bouncy Castle Library Downloader
echo Version: 1.78.1 for Java 17+
echo ========================================
echo.

REM Check if PowerShell is available
where powershell >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: PowerShell is not available on this system.
    echo Please download the libraries manually from:
    echo https://repo1.maven.org/maven2/org/bouncycastle/
    pause
    exit /b 1
)

REM Run the PowerShell script
echo Running PowerShell download script...
echo.
powershell -ExecutionPolicy Bypass -File "%~dp0download-bouncy-castle-libs.ps1"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Download failed. Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo Download completed successfully!
pause