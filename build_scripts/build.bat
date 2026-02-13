@echo off
REM Build script for PGP SupportPac with Java 17
REM This script compiles the Java source files and creates JAR files

setlocal enabledelayedexpansion

REM Set Java 17 home from IBM ACE installation
set "JAVA_HOME=C:\Program Files\IBM\ACE\13.0.6.0\common\java17"
set "ACE_HOME=C:\Program Files\IBM\ACE\13.0.6.0"

echo ========================================
echo PGP SupportPac Build Script (Java 17)
echo ========================================
echo.
echo Java Home: %JAVA_HOME%
echo ACE Home: %ACE_HOME%
echo.

REM Check if Java 17 is available
if not exist "%JAVA_HOME%\bin\javac.exe" (
    echo ERROR: Java 17 compiler not found at %JAVA_HOME%
    echo Please verify IBM ACE installation or update JAVA_HOME in this script.
    pause
    exit /b 1
)

REM Check Java version
echo Checking Java version...
"%JAVA_HOME%\bin\java.exe" -version
echo.

REM Check if Bouncy Castle libraries exist
echo Checking Bouncy Castle libraries...
set "BC_LIB_DIR=src\ACEv13\v2.0.1.0\PGPSupportPacImpl\lib"
if not exist "%BC_LIB_DIR%\bcpg-jdk18on-1.81.jar" (
    echo ERROR: Bouncy Castle libraries not found in %BC_LIB_DIR%
    echo Please run download-bouncy-castle-libs.bat first.
    pause
    exit /b 1
)
echo Bouncy Castle libraries found.
echo.

REM Create output directories
echo Creating output directories...
if not exist "build\PGPSupportPacImpl" mkdir "build\PGPSupportPacImpl"
if not exist "build\PGPSupportPac" mkdir "build\PGPSupportPac"
echo.

REM Compile PGPSupportPacImpl
echo ========================================
echo Compiling PGPSupportPacImpl...
echo ========================================
set "IMPL_SRC=src\ACEv13\v2.0.1.0\PGPSupportPacImpl\src"
set "IMPL_OUT=build\PGPSupportPacImpl"
set "IMPL_CP=%BC_LIB_DIR%\*;%ACE_HOME%\common\classes\IntegrationAPI.jar;%ACE_HOME%\server\classes\jplugin2.jar"

REM Find all Java files
dir /s /b "%IMPL_SRC%\*.java" > build\impl_sources.txt

"%JAVA_HOME%\bin\javac.exe" -cp "%IMPL_CP%" -d "%IMPL_OUT%" -encoding UTF-8 @build\impl_sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation of PGPSupportPacImpl failed.
    del build\impl_sources.txt
    pause
    exit /b 1
)
echo PGPSupportPacImpl compiled successfully.
del build\impl_sources.txt
echo.

REM Compile PGPSupportPac
echo ========================================
echo Compiling PGPSupportPac...
echo ========================================
set "PLUGIN_SRC=src\ACEv13\v2.0.1.0\PGPSupportPac\src"
set "PLUGIN_OUT=build\PGPSupportPac"
set "PLUGIN_CP=%ACE_HOME%\common\classes\IntegrationAPI.jar"

REM Find all Java files
dir /s /b "%PLUGIN_SRC%\*.java" > build\plugin_sources.txt

"%JAVA_HOME%\bin\javac.exe" -cp "%PLUGIN_CP%" -d "%PLUGIN_OUT%" -encoding UTF-8 @build\plugin_sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation of PGPSupportPac failed.
    del build\plugin_sources.txt
    pause
    exit /b 1
)
echo PGPSupportPac compiled successfully.
del build\plugin_sources.txt
echo.

REM Create JAR files
echo ========================================
echo Creating JAR files...
echo ========================================

REM Create PGPSupportPacImpl.jar
echo Creating PGPSupportPacImpl.jar...
cd build\PGPSupportPacImpl
"%JAVA_HOME%\bin\jar.exe" cvfm PGPSupportPacImpl.jar ^
    ..\..\src\ACEv13\v2.0.1.0\PGPSupportPacImpl\META-INF\MANIFEST.MF ^
    com pgpkeytool.class
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to create PGPSupportPacImpl.jar
    cd ..\..
    pause
    exit /b 1
)
cd ..\..

REM Copy to binary directory
if not exist "binary\ACEv13\lib" mkdir "binary\ACEv13\lib"
copy /Y "build\PGPSupportPacImpl\PGPSupportPacImpl.jar" "binary\ACEv13\lib\"
echo PGPSupportPacImpl.jar created and copied to binary\ACEv13\lib\
echo.

REM Create PGPSupportPac.jar (Plugin)
echo Creating PGPSupportPac.jar...
cd src\ACEv13\v2.0.1.0\PGPSupportPac

REM Copy compiled classes to com directory (merge with existing .msgnode and .properties files)
xcopy /E /Y ..\..\..\..\build\PGPSupportPac\com\ibm\broker\supportpac\pgp\*.class com\ibm\broker\supportpac\pgp\ >nul

REM The .msgnode and .properties files are already in the com directory from source
REM Now create the JAR with everything (includes .class, .msgnode, and .properties files)

"%JAVA_HOME%\bin\jar.exe" cvfm PGPSupportPac.jar ^
    META-INF\MANIFEST.MF ^
    com\ icons\ generated\ *.xml *.properties *.xmi .udnmetadata

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to create PGPSupportPac.jar
    REM Clean up only the copied .class files
    del /Q com\ibm\broker\supportpac\pgp\*.class 2>nul
    cd ..\..\..\..\
    pause
    exit /b 1
)

REM Clean up only the copied .class files (keep .msgnode and .properties)
del /Q com\ibm\broker\supportpac\pgp\*.class 2>nul

cd ..\..\..\..\

REM Copy to binary directory
if not exist "binary\ACEv13\plugins" mkdir "binary\ACEv13\plugins"
copy /Y "src\ACEv13\v2.0.1.0\PGPSupportPac\PGPSupportPac.jar" "binary\ACEv13\plugins\"
echo PGPSupportPac.jar created and copied to binary\ACEv13\plugins\
echo.

echo ========================================
echo Build Complete!
echo ========================================
echo.
echo Output files:
echo   - binary\ACEv13\lib\PGPSupportPacImpl.jar
echo   - binary\ACEv13\plugins\PGPSupportPac.jar
echo.
echo Next steps:
echo 1. Copy the JAR files to your IBM ACE installation
echo 2. Restart IBM ACE Toolkit
echo 3. Test the PGP nodes in your message flows
echo.
pause