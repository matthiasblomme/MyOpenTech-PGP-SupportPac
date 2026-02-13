@echo off
REM Deploy Maven-built JARs to ACE Integration Server and test
REM This script backs up existing files, deploys new ones, starts IS, and runs tests

setlocal enabledelayedexpansion

echo ========================================
echo PGP SupportPac - Deploy and Test
echo ========================================
echo.

REM Set paths
set "WORKSPACE_DIR=C:\Users\Bmatt\IBM\ACET13\workspacePgp"
set "TEST_SERVER=%WORKSPACE_DIR%\TEST_SERVER"
set "BACKUP_DIR=%WORKSPACE_DIR%\backup-original-jars"
set "ACE_BASE=C:\Program Files\IBM\ACE\13.0.6.0"

REM Determine MQSI paths
REM For standalone Integration Server, shared-classes is in the work directory
set "MQSI_BASE_FILEPATH=%ACE_BASE%"
set "MQSI_REGISTRY=%TEST_SERVER%"

REM Source files (Maven build output)
set "SRC_IMPL_JAR=binary\ACEv13\lib\PGPSupportPacImpl.jar"
set "SRC_PLUGIN_JAR=binary\ACEv13\plugins\PGPSupportPac.jar"
set "SRC_BCPG_JAR=src\ACEv13\v2.0.1.0\PGPSupportPacImpl\lib\bcpg-jdk18on-1.81.jar"
set "SRC_BCPROV_JAR=src\ACEv13\v2.0.1.0\PGPSupportPacImpl\lib\bcprov-jdk18on-1.81.jar"

REM Destination paths
set "DEST_IMPL=%MQSI_BASE_FILEPATH%\server\jplugin"
set "DEST_PLUGIN=%MQSI_BASE_FILEPATH%\tools\plugins"
set "DEST_SHARED=%MQSI_REGISTRY%\shared-classes"

echo Configuration:
echo   Workspace: %WORKSPACE_DIR%
echo   Test Server: %TEST_SERVER%
echo   ACE Base: %ACE_BASE%
echo   Backup Dir: %BACKUP_DIR%
echo.

REM Check if source files exist
echo Checking source files...
if not exist "%SRC_IMPL_JAR%" (
    echo ERROR: PGPSupportPacImpl.jar not found at %SRC_IMPL_JAR%
    echo Please run build-maven-java17.bat first
    exit /b 1
)
if not exist "%SRC_PLUGIN_JAR%" (
    echo ERROR: PGPSupportPac.jar not found at %SRC_PLUGIN_JAR%
    echo Please run build-maven-java17.bat first
    exit /b 1
)
echo   [OK] All source files found
echo.

REM Create backup directory
echo Creating backup directory...
if not exist "%BACKUP_DIR%" (
    mkdir "%BACKUP_DIR%"
    echo   [OK] Created %BACKUP_DIR%
) else (
    echo   [OK] Backup directory exists
)
echo.

REM Backup existing files
echo Backing up existing files...
set "BACKUP_COUNT=0"

if exist "%DEST_IMPL%\PGPSupportPacImpl.jar" (
    copy /Y "%DEST_IMPL%\PGPSupportPacImpl.jar" "%BACKUP_DIR%\" >nul 2>&1
    if !ERRORLEVEL! EQU 0 (
        echo   [OK] Backed up PGPSupportPacImpl.jar
        set /a BACKUP_COUNT+=1
    )
)

if exist "%DEST_PLUGIN%\PGPSupportPac.jar" (
    copy /Y "%DEST_PLUGIN%\PGPSupportPac.jar" "%BACKUP_DIR%\" >nul 2>&1
    if !ERRORLEVEL! EQU 0 (
        echo   [OK] Backed up PGPSupportPac.jar
        set /a BACKUP_COUNT+=1
    )
)

if exist "%DEST_SHARED%\bcpg-jdk18on-1.78.1.jar" (
    copy /Y "%DEST_SHARED%\bcpg-jdk18on-1.78.1.jar" "%BACKUP_DIR%\" >nul 2>&1
    if !ERRORLEVEL! EQU 0 (
        echo   [OK] Backed up bcpg-jdk18on-1.78.1.jar
        set /a BACKUP_COUNT+=1
    )
)

if exist "%DEST_SHARED%\bcprov-jdk18on-1.78.1.jar" (
    copy /Y "%DEST_SHARED%\bcprov-jdk18on-1.78.1.jar" "%BACKUP_DIR%\" >nul 2>&1
    if !ERRORLEVEL! EQU 0 (
        echo   [OK] Backed up bcprov-jdk18on-1.78.1.jar
        set /a BACKUP_COUNT+=1
    )
)

echo   Total files backed up: !BACKUP_COUNT!
echo.

REM Stop any running Integration Server
echo Checking for running Integration Server...
taskkill /F /IM IntegrationServer.exe >nul 2>&1
if !ERRORLEVEL! EQU 0 (
    echo   [INFO] Integration Server was running, stopped it
    timeout /t 2 /nobreak >nul
) else (
    echo   [OK] No Integration Server running
)
echo.

REM Deploy new files
echo Deploying Maven-built files...

echo   Deploying PGPSupportPacImpl.jar to %DEST_IMPL%...
copy /Y "%SRC_IMPL_JAR%" "%DEST_IMPL%\" >nul 2>&1
if !ERRORLEVEL! EQU 0 (
    echo   [OK] Deployed PGPSupportPacImpl.jar
) else (
    echo   [ERROR] Failed to deploy PGPSupportPacImpl.jar
    exit /b 1
)

echo   Deploying PGPSupportPac.jar to %DEST_PLUGIN%...
copy /Y "%SRC_PLUGIN_JAR%" "%DEST_PLUGIN%\" >nul 2>&1
if !ERRORLEVEL! EQU 0 (
    echo   [OK] Deployed PGPSupportPac.jar
) else (
    echo   [ERROR] Failed to deploy PGPSupportPac.jar
    exit /b 1
)

echo   Deploying Bouncy Castle libraries to %DEST_SHARED%...
copy /Y "%SRC_BCPG_JAR%" "%DEST_SHARED%\" >nul 2>&1
if !ERRORLEVEL! EQU 0 (
    echo   [OK] Deployed bcpg-jdk18on-1.81.jar
) else (
    echo   [ERROR] Failed to deploy bcpg-jdk18on-1.81.jar
    exit /b 1
)

copy /Y "%SRC_BCPROV_JAR%" "%DEST_SHARED%\" >nul 2>&1
if !ERRORLEVEL! EQU 0 (
    echo   [OK] Deployed bcprov-jdk18on-1.81.jar
) else (
    echo   [ERROR] Failed to deploy bcprov-jdk18on-1.81.jar
    exit /b 1
)

echo.
echo ========================================
echo Deployment Complete!
echo ========================================
echo.

REM Start Integration Server
echo Starting Integration Server...
call "%ACE_BASE%\server\bin\mqsiprofile.cmd" >nul 2>&1
start /B "" IntegrationServer --work-dir "%TEST_SERVER%" --console-log >nul 2>&1

REM Wait for server to start
echo Waiting for Integration Server to start...
set "MAX_WAIT=30"
set "WAIT_COUNT=0"

:WAIT_LOOP
timeout /t 2 /nobreak >nul
set /a WAIT_COUNT+=2

REM Check if server is responding
curl -s http://localhost:7800/pgp/encrypt >nul 2>&1
if !ERRORLEVEL! EQU 0 (
    echo   [OK] Integration Server started successfully ^(!WAIT_COUNT! seconds^)
    goto SERVER_READY
)

if !WAIT_COUNT! GEQ !MAX_WAIT! (
    echo   [ERROR] Integration Server failed to start within !MAX_WAIT! seconds
    echo   Please check the server logs
    exit /b 1
)

goto WAIT_LOOP

:SERVER_READY
echo.

REM Run tests
echo ========================================
echo Running PGP Tests
echo ========================================
echo.

echo Test 1: PGP Encryption
echo -----------------------
curl -X POST http://localhost:7800/pgp/encrypt
echo.
echo.

echo Test 2: PGP Decryption
echo -----------------------
curl -X POST http://localhost:7800/pgp/decrypt
echo.
echo.

echo ========================================
echo Tests Complete!
echo ========================================
echo.
echo Integration Server is still running.
echo To stop it, run: taskkill /F /IM IntegrationServer.exe
echo.
echo If you need to restore original files:
echo   Copy files from %BACKUP_DIR%
echo.

endlocal