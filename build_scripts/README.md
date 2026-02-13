# Build Scripts Guide

This directory contains scripts for building the PGP SupportPac with Java 17 and Bouncy Castle 1.81.

---

## Quick Start

### Prerequisites
- IBM ACE 13.0.6.0 or later installed
- Java 17 (included with ACE at `C:\Program Files\IBM\ACE\13.0.6.0\common\java17`)
- Bouncy Castle 1.81 libraries (auto-downloaded by scripts)

### Build Steps

```batch
# 1. Download Bouncy Castle libraries (first time only)
download-bouncy-castle-libs.bat

# 2. Build the project (run from PROJECT ROOT, not from build_scripts!)
build_scripts\build.bat

# 3. Deploy and test
deploy-and-test.bat
```

---

## Scripts Overview

### 1. `download-bouncy-castle-libs.bat`
**Purpose:** Downloads Bouncy Castle 1.81 libraries from Maven Central

**Run from:** Any directory (automatically navigates to project root)

**What it does:**
- Downloads `bcpg-jdk18on-1.81.jar` (PGP library)
- Downloads `bcprov-jdk18on-1.81.jar` (Crypto provider)
- Places files in:
  - `src/ACEv13/v2.0.1.0/PGPSupportPacImpl/lib/`
  - `binary/ACEv13/lib/`
- Removes old 1.78.1 versions
- **Note:** The PowerShell script automatically detects the project root and uses absolute paths, so it works correctly regardless of where you run it from

**When to run:**
- First time setup
- After upgrading Bouncy Castle version
- If library files are missing

**Example:**
```batch
cd build_scripts
download-bouncy-castle-libs.bat
```

---

### 2. `build.bat` ⚠️ IMPORTANT
**Purpose:** Compiles Java source and creates JAR files

**Run from:** **PROJECT ROOT DIRECTORY ONLY!**

**Why from root?**
The script uses relative paths like:
- `src\ACEv13\v2.0.1.0\PGPSupportPacImpl\src\*.java`
- `binary\ACEv13\lib\`

If run from `build_scripts\`, these paths won't exist!

**What it does:**
1. Checks Java 17 availability
2. Verifies Bouncy Castle libraries exist
3. Compiles `PGPSupportPacImpl` (23 Java files)
4. Compiles `PGPSupportPac` (2 Java files)
5. Creates JAR files with manifests
6. Copies to `binary/ACEv13/` directory

**Output files:**
- `binary/ACEv13/lib/PGPSupportPacImpl.jar` (~74 KB)
- `binary/ACEv13/plugins/PGPSupportPac.jar` (~47 KB)

**Example (CORRECT):**
```batch
# From project root
D:\GIT\MyOpenTech-PGP-SupportPac> build_scripts\build.bat
```

**Example (WRONG - DON'T DO THIS):**
```batch
# From build_scripts directory - WILL FAIL!
D:\GIT\MyOpenTech-PGP-SupportPac\build_scripts> build.bat
ERROR: Cannot find source files!
```

**Environment:**
- Uses: `C:\Program Files\IBM\ACE\13.0.6.0\common\java17`
- Classpath includes:
  - Bouncy Castle JARs
  - ACE IntegrationAPI.jar
  - ACE jplugin2.jar

---

### 3. `download-bouncy-castle-libs.ps1`
**Purpose:** PowerShell version of the download script

**Run from:** Any directory

**Usage:**
```powershell
powershell -ExecutionPolicy Bypass -File build_scripts\download-bouncy-castle-libs.ps1
```

---

## Common Issues and Solutions

### Issue 1: "Cannot find source files"

**Symptom:**
```
The system cannot find the file specified.
ERROR: Compilation of PGPSupportPacImpl failed.
```

**Cause:** Running `build.bat` from wrong directory

**Solution:**
```batch
# Make sure you're in project root
cd D:\GIT\MyOpenTech-PGP-SupportPac

# Then run
build_scripts\build.bat
```

### Issue 2: "Bouncy Castle libraries not found"

**Symptom:**
```
ERROR: Bouncy Castle libraries not found in src\ACEv13\v2.0.1.0\PGPSupportPacImpl\lib
Please run download-bouncy-castle-libs.bat first.
```

**Solution:**
```batch
cd build_scripts
download-bouncy-castle-libs.bat
cd ..
build_scripts\build.bat
```

### Issue 3: "Java 17 compiler not found"

**Symptom:**
```
ERROR: Java 17 compiler not found at C:\Program Files\IBM\ACE\13.0.6.0\common\java17
```

**Solution:**
1. Verify ACE installation path
2. Update `JAVA_HOME` in `build.bat` if ACE is installed elsewhere:
   ```batch
   set "JAVA_HOME=C:\Your\ACE\Path\common\java17"
   ```

### Issue 4: Build succeeds but JARs are old

**Symptom:** JAR files have old timestamps

**Cause:** Build output went to wrong location

**Solution:**
1. Delete old JARs:
   ```batch
   del binary\ACEv13\lib\PGPSupportPacImpl.jar
   del binary\ACEv13\plugins\PGPSupportPac.jar
   ```
2. Rebuild from project root:
   ```batch
   build_scripts\build.bat
   ```
3. Verify timestamps:
   ```batch
   dir binary\ACEv13\lib\*.jar /T:W
   ```

---

## Build Process Details

### Compilation Steps

#### Step 1: Compile PGPSupportPacImpl
```batch
javac -cp "BC_LIBS;ACE_LIBS" -d build\PGPSupportPacImpl -encoding UTF-8 @sources.txt
```

**Classpath includes:**
- `bcpg-jdk18on-1.81.jar`
- `bcprov-jdk18on-1.81.jar`
- `IntegrationAPI.jar`
- `jplugin2.jar`

**Output:** 23 compiled `.class` files

#### Step 2: Create PGPSupportPacImpl.jar
```batch
jar cvfm PGPSupportPacImpl.jar MANIFEST.MF com pgpkeytool.class
```

**Includes:**
- All compiled classes
- `META-INF/MANIFEST.MF`

#### Step 3: Compile PGPSupportPac
```batch
javac -cp "ACE_LIBS" -d build\PGPSupportPac -encoding UTF-8 @sources.txt
```

**Output:** 2 compiled `.class` files (UDN wrappers)

#### Step 4: Create PGPSupportPac.jar
```batch
jar cvfm PGPSupportPac.jar MANIFEST.MF com\ icons\ generated\ *.xml *.properties
```

**Includes:**
- Compiled classes
- `.msgnode` files
- `.properties` files
- Icons
- Plugin metadata

---

## Directory Structure

```
MyOpenTech-PGP-SupportPac/          ← RUN build.bat FROM HERE!
├── build_scripts/
│   ├── build.bat                    ← Main build script
│   ├── download-bouncy-castle-libs.bat
│   ├── download-bouncy-castle-libs.ps1
│   └── README.md                    ← This file
├── src/
│   └── ACEv13/v2.0.1.0/
│       ├── PGPSupportPacImpl/
│       │   ├── lib/
│       │   │   ├── bcpg-jdk18on-1.81.jar
│       │   │   └── bcprov-jdk18on-1.81.jar
│       │   └── src/                 ← Java source files
│       └── PGPSupportPac/
│           └── src/                 ← Plugin source files
├── binary/
│   └── ACEv13/
│       ├── lib/
│       │   ├── PGPSupportPacImpl.jar    ← Build output
│       │   ├── bcpg-jdk18on-1.81.jar
│       │   └── bcprov-jdk18on-1.81.jar
│       └── plugins/
│           └── PGPSupportPac.jar        ← Build output
└── build/                           ← Temporary build directory
    ├── PGPSupportPacImpl/           ← Compiled classes
    └── PGPSupportPac/               ← Compiled classes
```

---

## Build Verification

### Check Build Success

```batch
# 1. Verify JAR files exist
dir binary\ACEv13\lib\PGPSupportPacImpl.jar
dir binary\ACEv13\plugins\PGPSupportPac.jar

# 2. Check file sizes
# PGPSupportPacImpl.jar should be ~74 KB
# PGPSupportPac.jar should be ~47 KB

# 3. Verify timestamps (should be recent)
dir binary\ACEv13\lib\*.jar /T:W
dir binary\ACEv13\plugins\*.jar /T:W

# 4. Check JAR contents
jar tf binary\ACEv13\lib\PGPSupportPacImpl.jar | more
jar tf binary\ACEv13\plugins\PGPSupportPac.jar | more
```

### Expected Output

```
PGPSupportPacImpl.jar contents:
META-INF/MANIFEST.MF
com/ibm/broker/supportpac/pgp/PGPDecrypter.class
com/ibm/broker/supportpac/pgp/PGPEncrypter.class
... (23 classes total)

PGPSupportPac.jar contents:
META-INF/MANIFEST.MF
com/ibm/broker/supportpac/pgp/PGPDecrypterNodeUDN.class
com/ibm/broker/supportpac/pgp/PGPEncrypterNodeUDN.class
com/ibm/broker/supportpac/pgp/PGPDecrypter.msgnode
com/ibm/broker/supportpac/pgp/PGPEncrypter.msgnode
icons/...
plugin.xml
```

---

## Clean Build

To perform a clean build:

```batch
# 1. Delete build directory
rmdir /S /Q build

# 2. Delete old JARs
del binary\ACEv13\lib\PGPSupportPacImpl.jar
del binary\ACEv13\plugins\PGPSupportPac.jar

# 3. Rebuild
build_scripts\build.bat
```

---

## Troubleshooting Checklist

Before asking for help, verify:

- [ ] Running from project root directory
- [ ] Java 17 is available at expected path
- [ ] Bouncy Castle 1.81 JARs exist in `src/.../lib/`
- [ ] ACE is installed at `C:\Program Files\IBM\ACE\13.0.6.0`
- [ ] No spaces in project path
- [ ] Windows user has write permissions
- [ ] Antivirus not blocking JAR creation
- [ ] Previous build artifacts cleaned up

---

## Advanced: Customizing Build

### Change Java Version

Edit `build.bat`:
```batch
set "JAVA_HOME=C:\Path\To\Your\Java17"
```

### Change ACE Installation Path

Edit `build.bat`:
```batch
set "ACE_HOME=C:\Your\ACE\Installation"
```

### Change Bouncy Castle Version

1. Edit `download-bouncy-castle-libs.ps1`:
   ```powershell
   $BC_VERSION = "1.XX"
   ```

2. Edit `build.bat`:
   ```batch
   if not exist "%BC_LIB_DIR%\bcpg-jdk18on-1.XX.jar" (
   ```

3. Download new libraries:
   ```batch
   cd build_scripts
   download-bouncy-castle-libs.bat
   ```

4. Rebuild:
   ```batch
   cd ..
   build_scripts\build.bat
   ```

---

## Integration with CI/CD

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any
    stages {
        stage('Download Dependencies') {
            steps {
                bat 'build_scripts\\download-bouncy-castle-libs.bat'
            }
        }
        stage('Build') {
            steps {
                bat 'build_scripts\\build.bat'
            }
        }
        stage('Test') {
            steps {
                bat 'deploy-and-test.bat'
            }
        }
    }
}
```

### GitHub Actions Example

```yaml
name: Build PGP SupportPac
on: [push]
jobs:
  build:
    runs-on: windows-latest
    steps:
    - uses: actions/checkout@v2
    - name: Download BC Libraries
      run: build_scripts\download-bouncy-castle-libs.bat
    - name: Build
      run: build_scripts\build.bat
    - name: Upload Artifacts
      uses: actions/upload-artifact@v2
      with:
        name: pgp-supportpac-jars
        path: binary/ACEv13/**/*.jar
```

---

## FAQ

**Q: Why not use Maven?**  
A: The `build.bat` script is simpler and doesn't require Maven installation. Maven support is available via `build-maven-java17.bat` if preferred.

**Q: Can I build on Linux?**  
A: Yes, but you'll need to create equivalent shell scripts. The Java compilation commands are the same.

**Q: Do I need to rebuild after changing Java code?**  
A: Yes, run `build_scripts\build.bat` from project root after any code changes.

**Q: How do I know which BC version to use?**  
A: Match your target environment. For IBM ACE containers, use BC 1.81.

**Q: Can I build without ACE installed?**  
A: No, the build requires ACE's `IntegrationAPI.jar` and `jplugin2.jar`. However, you can copy these JARs to a `lib/` directory and update the classpath in `build.bat`.

---

## Support

For issues or questions:
1. Check this README
2. Review build output for error messages
3. Verify all prerequisites are met
4. Check project documentation in `docs/`

---

**Last Updated:** 2026-02-13  
**Bouncy Castle Version:** 1.81  
**Java Version:** 17  
**ACE Version:** 13.0.6.0