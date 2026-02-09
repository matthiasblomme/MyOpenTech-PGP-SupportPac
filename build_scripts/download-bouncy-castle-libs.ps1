# PowerShell script to download Bouncy Castle libraries for Java 17
# Run this script to automatically download the required JAR files

$ErrorActionPreference = "Stop"

# Define library versions and URLs
$BC_VERSION = "1.78.1"
$MAVEN_BASE_URL = "https://repo1.maven.org/maven2/org/bouncycastle"

$libraries = @(
    @{
        Name = "bcpg-jdk18on"
        Version = $BC_VERSION
        FileName = "bcpg-jdk18on-$BC_VERSION.jar"
        Url = "$MAVEN_BASE_URL/bcpg-jdk18on/$BC_VERSION/bcpg-jdk18on-$BC_VERSION.jar"
    },
    @{
        Name = "bcprov-jdk18on"
        Version = $BC_VERSION
        FileName = "bcprov-jdk18on-$BC_VERSION.jar"
        Url = "$MAVEN_BASE_URL/bcprov-jdk18on/$BC_VERSION/bcprov-jdk18on-$BC_VERSION.jar"
    }
)

# Define target directories
$targetDirs = @(
    "src\ACEv13\v2.0.1.0\PGPSupportPacImpl\lib",
    "binary\ACEv13\lib"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Bouncy Castle Library Downloader" -ForegroundColor Cyan
Write-Host "Version: $BC_VERSION for Java 17+" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Create directories if they don't exist
foreach ($dir in $targetDirs) {
    if (-not (Test-Path $dir)) {
        Write-Host "Creating directory: $dir" -ForegroundColor Yellow
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
}

# Download libraries
foreach ($lib in $libraries) {
    Write-Host "Downloading $($lib.Name) v$($lib.Version)..." -ForegroundColor Green
    
    foreach ($dir in $targetDirs) {
        $targetPath = Join-Path $dir $lib.FileName
        
        try {
            Write-Host "  -> $targetPath" -ForegroundColor Gray
            Invoke-WebRequest -Uri $lib.Url -OutFile $targetPath -UseBasicParsing
            
            # Verify file was downloaded
            if (Test-Path $targetPath) {
                $fileSize = (Get-Item $targetPath).Length / 1MB
                Write-Host "     Downloaded successfully ($([math]::Round($fileSize, 2)) MB)" -ForegroundColor Green
            }
        }
        catch {
            Write-Host "     ERROR: Failed to download - $($_.Exception.Message)" -ForegroundColor Red
            exit 1
        }
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Cleaning up old libraries..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Remove old Bouncy Castle libraries
$oldLibs = @(
    "bcpg-jdk15to18-170.jar",
    "bcprov-ext-jdk15to18-170.jar",
    "bcprov-jdk15to18-170.jar",
    "bcpg-jdk15on-154.jar",
    "bcprov-ext-jdk15on-154.jar",
    "bcprov-jdk15on-154.jar"
)

foreach ($dir in $targetDirs) {
    foreach ($oldLib in $oldLibs) {
        $oldPath = Join-Path $dir $oldLib
        if (Test-Path $oldPath) {
            Write-Host "Removing old library: $oldPath" -ForegroundColor Yellow
            Remove-Item $oldPath -Force
            Write-Host "  Removed successfully" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Download Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Verify the downloaded JAR files in the lib directories" -ForegroundColor White
Write-Host "2. Rebuild the project using IntelliJ IDEA or the compilation scripts" -ForegroundColor White
Write-Host "3. Test the PGP encryption/decryption functionality" -ForegroundColor White
Write-Host ""
Write-Host "For detailed instructions, see JAVA17_UPGRADE_NOTES.md" -ForegroundColor Cyan