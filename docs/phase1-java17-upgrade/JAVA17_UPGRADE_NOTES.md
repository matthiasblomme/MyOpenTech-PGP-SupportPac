# Java 17 Upgrade Notes for PGP SupportPac

## Overview
This document describes the changes made to upgrade the PGP SupportPac project from Java 7 to Java 17.

## Changes Made

### 1. MANIFEST.MF Updates

#### PGPSupportPac Plugin (src/ACEv13/v2.0.1.0/PGPSupportPac/META-INF/MANIFEST.MF)
- Updated `Bundle-RequiredExecutionEnvironment` from `JavaSE-1.7` to `JavaSE-17`

#### PGPSupportPacImpl (src/ACEv13/v2.0.1.0/PGPSupportPacImpl/META-INF/MANIFEST.MF)
- Updated `Class-Path` to reference new Bouncy Castle libraries:
  - Old: `bcpg-jdk15on-154.jar bcprov-ext-jdk15on-154.jar`
  - New: `bcpg-jdk18on-1.78.1.jar bcprov-jdk18on-1.78.1.jar`

### 2. Bouncy Castle Library Upgrade

The Bouncy Castle cryptography libraries need to be updated to support Java 17:

#### Required Libraries
You need to download and replace the following JAR files in these directories:
- `src/ACEv13/v2.0.1.0/PGPSupportPacImpl/lib/`
- `binary/ACEv13/lib/`

#### Download Links (Maven Central)
1. **bcpg-jdk18on-1.78.1.jar** (Bouncy Castle OpenPGP API)
   - Maven: `org.bouncycastle:bcpg-jdk18on:1.78.1`
   - URL: https://repo1.maven.org/maven2/org/bouncycastle/bcpg-jdk18on/1.78.1/bcpg-jdk18on-1.78.1.jar

2. **bcprov-jdk18on-1.78.1.jar** (Bouncy Castle Provider)
   - Maven: `org.bouncycastle:bcprov-jdk18on:1.78.1`
   - URL: https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk18on/1.78.1/bcprov-jdk18on-1.78.1.jar

#### Files to Remove
Remove the old Bouncy Castle libraries:
- `bcpg-jdk15to18-170.jar`
- `bcprov-ext-jdk15to18-170.jar`
- `bcprov-jdk15to18-170.jar`

### 3. IntelliJ IDEA Module Configuration

The IntelliJ IDEA module files (.iml) already have `LANGUAGE_LEVEL="JDK_17"` configured:
- `src/ACEv13/v2.0.1.0/PGPSupportPac/PGPSupportPac.iml`
- `src/ACEv13/v2.0.1.0/PGPSupportPacImpl/PGPSupportPacImpl.iml`
- `MyOpenTech-PGP-SupportPac.iml`

### 4. Java Runtime Configuration

The project is configured to use IBM ACE's Java 17 runtime:
- Runtime Path: `C:\Program Files\IBM\ACE\13.0.6.0\common\java17`

Ensure this path is correctly set in your IntelliJ IDEA project settings:
1. File → Project Structure → Project → SDK
2. Select or add the Java 17 SDK from the ACE installation

### 5. Source Code Compatibility

The Java source code has been reviewed and is compatible with Java 17:
- No deprecated API usage that would break in Java 17
- Serialization uses `serialVersionUID` correctly
- No use of removed APIs from Java 7 to Java 17

## Compilation Instructions

### Using IntelliJ IDEA
1. Open the project in IntelliJ IDEA
2. Ensure Java 17 SDK is configured (from ACE installation)
3. Download and place the new Bouncy Castle JARs in the lib directories
4. Build → Rebuild Project
### Using Visual Studio Code

#### Prerequisites
1. Install the following VSCode extensions:
   - **Extension Pack for Java** (by Microsoft) - includes:
     - Language Support for Java(TM) by Red Hat
     - Debugger for Java
     - Test Runner for Java
     - Maven for Java
     - Project Manager for Java
   - **Java Dependency Viewer** (optional, for managing dependencies)

#### Configuration Steps

1. **Configure Java 17 SDK**
   - Open Command Palette (Ctrl+Shift+P / Cmd+Shift+P)
   - Type "Java: Configure Java Runtime"
   - Add or select Java 17 from ACE installation: `C:\Program Files\IBM\ACE\13.0.6.0\common\java17`
   - Set it as the default Java runtime for the workspace

2. **Configure Workspace Settings**
   
   Create or update `.vscode/settings.json` in the project root:
   ```json
   {
     "java.configuration.runtimes": [
       {
         "name": "JavaSE-17",
         "path": "C:\\Program Files\\IBM\\ACE\\13.0.6.0\\common\\java17",
         "default": true
       }
     ],
     "java.project.sourcePaths": [
       "src/ACEv13/v2.0.1.0/PGPSupportPac/src",
       "src/ACEv13/v2.0.1.0/PGPSupportPacImpl/src"
     ],
     "java.project.outputPath": "out",
     "java.project.referencedLibraries": [
       "src/ACEv13/v2.0.1.0/PGPSupportPacImpl/lib/*.jar",
       "C:\\Program Files\\IBM\\ACE\\13.0.6.0\\common\\classes\\IntegrationAPI.jar",
       "C:\\Program Files\\IBM\\ACE\\13.0.6.0\\server\\classes\\jplugin2.jar"
     ]
   }
   ```

3. **Download and Place Bouncy Castle JARs**
   - Download the new Bouncy Castle JARs (see section 2 above)
   - Place them in `src/ACEv13/v2.0.1.0/PGPSupportPacImpl/lib/`
   - VSCode will automatically detect them based on the settings

4. **Build the Project**
   - Open Command Palette (Ctrl+Shift+P / Cmd+Shift+P)
   - Type "Java: Clean Java Language Server Workspace" (first time only)
   - Type "Java: Force Java Compilation" or use "Java: Build Workspace"
   - Alternatively, save any Java file to trigger automatic compilation

5. **Verify Compilation**
   - Check the "Problems" panel (Ctrl+Shift+M / Cmd+Shift+M) for any errors
   - Compiled classes will be in the `out` directory

#### Building JAR Files in VSCode

You can use the integrated terminal in VSCode to build JAR files:

1. **Open Integrated Terminal** (Ctrl+` / Cmd+`)

2. **Build PGPSupportPacImpl.jar**
   ```bash
   cd src/ACEv13/v2.0.1.0/PGPSupportPacImpl
   jar cvfm PGPSupportPacImpl.jar META-INF/MANIFEST.MF -C ../../../out/PGPSupportPacImpl .
   copy PGPSupportPacImpl.jar ..\..\..\..\binary\ACEv13\lib\
   ```

3. **Build PGPSupportPac.jar**
   ```bash
   cd src/ACEv13/v2.0.1.0/PGPSupportPac
   jar cvfm PGPSupportPac.jar META-INF/MANIFEST.MF -C ../../../out/PGPSupportPac . com/ icons/ *.xml *.properties *.xmi
   copy PGPSupportPac.jar ..\..\..\..\binary\ACEv13\plugins\
   ```

#### Troubleshooting VSCode Compilation

**Issue: Java Language Server not recognizing Java 17**
- Solution: Reload the window (Command Palette → "Developer: Reload Window")
- Ensure Java 17 is properly installed and path is correct

**Issue: Cannot find ACE libraries**
- Solution: Verify the paths in `.vscode/settings.json` match your ACE installation
- Use forward slashes or escaped backslashes in JSON paths

**Issue: Bouncy Castle JARs not recognized**
- Solution: After adding JARs, run "Java: Clean Java Language Server Workspace"
- Check that JARs are in the correct directory specified in settings

**Issue: Compilation errors in Problems panel**
- Solution: Ensure all dependencies are correctly referenced
- Check that source paths include both PGPSupportPac and PGPSupportPacImpl


### Manual Compilation
```bash
# Set JAVA_HOME to ACE's Java 17
set JAVA_HOME=C:\Program Files\IBM\ACE\13.0.6.0\common\java17

# Compile PGPSupportPacImpl
cd src/ACEv13/v2.0.1.0/PGPSupportPacImpl
"%JAVA_HOME%\bin\javac" -cp "lib/*;C:\Program Files\IBM\ACE\13.0.6.0\common\classes\IntegrationAPI.jar;C:\Program Files\IBM\ACE\13.0.6.0\server\classes\jplugin2.jar" -d out src/com/ibm/broker/supportpac/pgp/*.java src/com/ibm/broker/supportpac/pgp/impl/*.java

# Compile PGPSupportPac
cd ../PGPSupportPac
"%JAVA_HOME%\bin\javac" -cp "C:\Program Files\IBM\ACE\13.0.6.0\common\classes\IntegrationAPI.jar" -d out src/com/ibm/broker/supportpac/pgp/*.java
```

## Building JAR Files

After compilation, rebuild the JAR files:

### PGPSupportPacImpl.jar
```bash
cd src/ACEv13/v2.0.1.0/PGPSupportPacImpl
jar cvfm PGPSupportPacImpl.jar META-INF/MANIFEST.MF -C out .
copy PGPSupportPacImpl.jar ..\..\..\..\binary\ACEv13\lib\
```

### PGPSupportPac.jar (Plugin)
```bash
cd src/ACEv13/v2.0.1.0/PGPSupportPac
jar cvfm PGPSupportPac.jar META-INF/MANIFEST.MF -C out . com/ icons/ *.xml *.properties *.xmi
copy PGPSupportPac.jar ..\..\..\..\binary\ACEv13\plugins\
```

## Testing

After upgrading:
1. Deploy the updated plugin to IBM ACE Toolkit
2. Test PGP encryption/decryption operations
3. Verify signature validation works correctly
4. Test with various PGP key types and algorithms

## Known Issues and Considerations

### Bouncy Castle API Changes
The Bouncy Castle 1.78.1 API is backward compatible with the code, but be aware:
- Some deprecated methods may have warnings
- Security algorithms may have different default behaviors
- Performance characteristics may differ

### Java 17 Features
While the code is compatible with Java 17, it doesn't yet use new features like:
- Records
- Pattern matching
- Text blocks
- Sealed classes

Consider refactoring to use these features in future updates.

## Rollback Instructions

If you need to rollback to Java 7:
1. Restore the original MANIFEST.MF files from git
2. Replace Bouncy Castle JARs with the original versions
3. Update IntelliJ .iml files to `LANGUAGE_LEVEL="JDK_1_7"`
4. Rebuild the project

## Support

For issues related to this upgrade, contact the project maintainer or refer to:
- IBM ACE Documentation: https://www.ibm.com/docs/en/app-connect/
- Bouncy Castle Documentation: https://www.bouncycastle.org/documentation.html