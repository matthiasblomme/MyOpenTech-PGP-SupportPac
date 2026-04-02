@echo off
REM Build script for PGP SupportPac with Java 17
REM Sets up ACE environment and runs Maven build

echo ========================================
echo PGP SupportPac - Maven Build (Java 17)
echo ========================================
echo.

REM Set ACE environment
set "ACE_BASE=C:\Program Files\IBM\ACE\13.0.6.0"
echo Setting up ACE environment...
call "%ACE_BASE%\server\bin\mqsiprofile.cmd"

REM Override with Java 17 (ACE profile sets Java 8 by default)
set "JAVA17_HOME=%ACE_BASE%\common\java17"
set "JAVA_HOME=%JAVA17_HOME%"
set "PATH=%JAVA17_HOME%\bin;%PATH%"

REM Verify Java version
echo Checking Java version...
"%JAVA_HOME%\bin\java" -version
echo.

REM Check if Maven is available
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven not found in PATH
    echo Please install Maven or add it to your PATH
    exit /b 1
)

echo Maven version:
mvn --version
echo.

REM Run Maven build
echo Starting Maven build...
echo.

mvn clean install -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL
    echo ========================================
    echo.
    echo Output JARs:
    echo - binary/ACEv13/lib/PGPSupportPacImpl.jar
    echo - binary/ACEv13/plugins/PGPSupportPac.jar
    echo.
) else (
    echo.
    echo ========================================
    echo BUILD FAILED
    echo ========================================
    echo.
    exit /b 1
)