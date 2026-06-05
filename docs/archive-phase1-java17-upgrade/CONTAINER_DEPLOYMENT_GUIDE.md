# Container Deployment Guide - Bouncy Castle 1.81

**Date:** 2026-02-13  
**Version:** PGP SupportPac 2.0.1.0 with BC 1.81  
**Target:** IBM ACE Container Images

---

## Overview

This guide explains how to deploy the PGP SupportPac to IBM ACE container images that come with Bouncy Castle 1.81 pre-installed.

### The Container Environment

IBM ACE container images include:
```
/opt/ibm/ace-13/common/classes/
├── bcprov-jdk18on.jar   (version 1.81) ✅
├── bcpkix-jdk18on.jar   (version 1.81) ✅
└── bcutil-jdk18on.jar   (version 1.81) ✅
```

**What's Missing:** `bcpg-jdk18on.jar` (PGP library) ❌

### Our Solution

We upgraded the PGP SupportPac to use BC 1.81 to match the container environment exactly, and we provide the missing `bcpg-jdk18on-1.81.jar` library.

---

## Deployment Files

### Required Files

From the `binary/ACEv13` directory:

1. **`lib/PGPSupportPacImpl.jar`** (74,310 bytes)
   - Core PGP implementation
   - Compiled with BC 1.81
   - Deploy to: `/opt/ibm/ace-13/server/jplugin/`

2. **`plugins/PGPSupportPac.jar`** (47,503 bytes)
   - ACE Toolkit plugin
   - Deploy to: `/opt/ibm/ace-13/tools/plugins/`

3. **`lib/bcpg-jdk18on-1.81.jar`** (728,364 bytes)
   - Bouncy Castle PGP library
   - **CRITICAL:** Must match container BC version (1.81)
   - Deploy to: Integration Server's `shared-classes` directory

### Optional Files

4. **`lib/bcprov-jdk18on-1.81.jar`** (8,948,201 bytes)
   - Bouncy Castle provider
   - **NOT NEEDED** - Container already has this
   - Only include if you want to override container's version

---

## Deployment Methods

### Method 1: Dockerfile (Recommended)

```dockerfile
FROM cp.icr.io/cp/appc/ace:13.0.6.0-r1

# Switch to root to copy files
USER root

# Copy PGP SupportPac files
COPY binary/ACEv13/lib/PGPSupportPacImpl.jar /opt/ibm/ace-13/server/jplugin/
COPY binary/ACEv13/plugins/PGPSupportPac.jar /opt/ibm/ace-13/tools/plugins/
COPY binary/ACEv13/lib/bcpg-jdk18on-1.81.jar /home/aceuser/ace-server/shared-classes/

# Set correct permissions
RUN chown aceuser:aceuser /opt/ibm/ace-13/server/jplugin/PGPSupportPacImpl.jar && \
    chown aceuser:aceuser /opt/ibm/ace-13/tools/plugins/PGPSupportPac.jar && \
    chown aceuser:aceuser /home/aceuser/ace-server/shared-classes/bcpg-jdk18on-1.81.jar

# Switch back to aceuser
USER aceuser

# Your application deployment continues here...
```

### Method 2: Kubernetes ConfigMap/Volume

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: pgp-supportpac-libs
data:
  # Base64 encode the JAR files or use binaryData
binaryData:
  PGPSupportPacImpl.jar: <base64-encoded-content>
  PGPSupportPac.jar: <base64-encoded-content>
  bcpg-jdk18on-1.81.jar: <base64-encoded-content>
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ace-integration-server
spec:
  template:
    spec:
      containers:
      - name: ace
        image: cp.icr.io/cp/appc/ace:13.0.6.0-r1
        volumeMounts:
        - name: pgp-libs
          mountPath: /opt/ibm/ace-13/server/jplugin/PGPSupportPacImpl.jar
          subPath: PGPSupportPacImpl.jar
        - name: pgp-libs
          mountPath: /opt/ibm/ace-13/tools/plugins/PGPSupportPac.jar
          subPath: PGPSupportPac.jar
        - name: pgp-libs
          mountPath: /home/aceuser/ace-server/shared-classes/bcpg-jdk18on-1.81.jar
          subPath: bcpg-jdk18on-1.81.jar
      volumes:
      - name: pgp-libs
        configMap:
          name: pgp-supportpac-libs
```

### Method 3: Init Container

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ace-integration-server
spec:
  template:
    spec:
      initContainers:
      - name: install-pgp-supportpac
        image: busybox
        command:
        - sh
        - -c
        - |
          cp /pgp-libs/PGPSupportPacImpl.jar /jplugin/
          cp /pgp-libs/PGPSupportPac.jar /plugins/
          cp /pgp-libs/bcpg-jdk18on-1.81.jar /shared-classes/
        volumeMounts:
        - name: pgp-libs
          mountPath: /pgp-libs
        - name: jplugin
          mountPath: /jplugin
        - name: plugins
          mountPath: /plugins
        - name: shared-classes
          mountPath: /shared-classes
      containers:
      - name: ace
        image: cp.icr.io/cp/appc/ace:13.0.6.0-r1
        volumeMounts:
        - name: jplugin
          mountPath: /opt/ibm/ace-13/server/jplugin
        - name: plugins
          mountPath: /opt/ibm/ace-13/tools/plugins
        - name: shared-classes
          mountPath: /home/aceuser/ace-server/shared-classes
      volumes:
      - name: pgp-libs
        configMap:
          name: pgp-supportpac-libs
      - name: jplugin
        emptyDir: {}
      - name: plugins
        emptyDir: {}
      - name: shared-classes
        emptyDir: {}
```

---

## Verification

### 1. Check Files Are Deployed

```bash
# Connect to the container
kubectl exec -it <pod-name> -- /bin/bash

# Verify files exist
ls -lh /opt/ibm/ace-13/server/jplugin/PGPSupportPacImpl.jar
ls -lh /opt/ibm/ace-13/tools/plugins/PGPSupportPac.jar
ls -lh /home/aceuser/ace-server/shared-classes/bcpg-jdk18on-1.81.jar

# Check file sizes
# PGPSupportPacImpl.jar should be ~74KB
# PGPSupportPac.jar should be ~47KB
# bcpg-jdk18on-1.81.jar should be ~728KB
```

### 2. Verify Bouncy Castle Version

```bash
# Check container's BC version
unzip -p /opt/ibm/ace-13/common/classes/bcprov-jdk18on.jar META-INF/MANIFEST.MF | grep Implementation-Version

# Should show: Implementation-Version: 1.81

# Check our PGP library version
unzip -p /home/aceuser/ace-server/shared-classes/bcpg-jdk18on-1.81.jar META-INF/MANIFEST.MF | grep Implementation-Version

# Should show: Implementation-Version: 1.81
```

### 3. Test PGP Functionality

Deploy a test message flow and verify:

```bash
# Test encryption
curl -X POST http://<integration-server>:7800/pgp/encrypt

# Expected: PGP encrypted message starting with "-----BEGIN PGP MESSAGE-----"

# Test decryption
curl -X POST http://<integration-server>:7800/pgp/decrypt

# Expected: Decrypted plaintext content
```

---

## Troubleshooting

### Issue 1: ClassNotFoundException for PGP Classes

**Symptom:**
```
java.lang.ClassNotFoundException: org.bouncycastle.openpgp.PGPPublicKey
```

**Cause:** `bcpg-jdk18on-1.81.jar` not in shared-classes

**Solution:**
```bash
# Verify the file exists
ls -lh /home/aceuser/ace-server/shared-classes/bcpg-jdk18on-1.81.jar

# If missing, redeploy the JAR file
```

### Issue 2: Version Mismatch Errors

**Symptom:**
```
java.lang.NoSuchMethodError: org.bouncycastle.crypto.CryptoServicesRegistrar
```

**Cause:** BC version mismatch between container and plugin

**Solution:**
1. Verify container BC version:
   ```bash
   unzip -p /opt/ibm/ace-13/common/classes/bcprov-jdk18on.jar META-INF/MANIFEST.MF | grep Implementation-Version
   ```

2. Ensure our `bcpg-jdk18on-1.81.jar` matches that version

3. Rebuild plugin if necessary with matching BC version

### Issue 3: Permission Denied

**Symptom:**
```
java.io.FileNotFoundException: /opt/ibm/ace-13/server/jplugin/PGPSupportPacImpl.jar (Permission denied)
```

**Solution:**
```dockerfile
# In Dockerfile, ensure correct ownership
RUN chown aceuser:aceuser /opt/ibm/ace-13/server/jplugin/PGPSupportPacImpl.jar && \
    chmod 644 /opt/ibm/ace-13/server/jplugin/PGPSupportPacImpl.jar
```

### Issue 4: Integration Server Won't Start

**Symptom:** Server fails to start after deploying PGP SupportPac

**Diagnosis:**
```bash
# Check Integration Server logs
cat /home/aceuser/ace-server/logs/integration_server.txt

# Look for errors related to:
# - ClassNotFoundException
# - NoClassDefFoundError
# - UnsatisfiedLinkError
```

**Common Causes:**
1. Missing `bcpg-jdk18on-1.81.jar`
2. Corrupted JAR files
3. Wrong BC version
4. Incorrect file permissions

---

## Best Practices

### 1. Version Consistency

✅ **DO:**
- Always match BC version between container and plugin
- Use BC 1.81 for current IBM ACE containers
- Document BC version in your deployment

❌ **DON'T:**
- Mix BC versions (e.g., 1.78.1 plugin with 1.81 container)
- Override container's `bcprov-jdk18on.jar` unless necessary
- Deploy without version verification

### 2. File Organization

```
Container Structure:
/opt/ibm/ace-13/
├── common/classes/
│   ├── bcprov-jdk18on.jar    (Container's BC 1.81)
│   ├── bcpkix-jdk18on.jar    (Container's BC 1.81)
│   └── bcutil-jdk18on.jar    (Container's BC 1.81)
├── server/jplugin/
│   └── PGPSupportPacImpl.jar (Our implementation)
└── tools/plugins/
    └── PGPSupportPac.jar     (Our plugin)

/home/aceuser/ace-server/
└── shared-classes/
    └── bcpg-jdk18on-1.81.jar (Our PGP library)
```

### 3. Health Checks

Add health checks to verify PGP functionality:

```yaml
livenessProbe:
  httpGet:
    path: /health
    port: 7800
  initialDelaySeconds: 60
  periodSeconds: 30

readinessProbe:
  httpGet:
    path: /ready
    port: 7800
  initialDelaySeconds: 30
  periodSeconds: 10
```

### 4. Monitoring

Monitor for PGP-related issues:

```yaml
# Prometheus metrics example
- alert: PGPEncryptionFailure
  expr: rate(pgp_encryption_errors_total[5m]) > 0
  annotations:
    summary: "PGP encryption failures detected"
    description: "Check BC library versions and permissions"
```

---

## Container-Specific Considerations

### IBM Cloud Pak for Integration

When deploying to CP4I:

1. **Use Integration Server custom resource:**
   ```yaml
   apiVersion: appconnect.ibm.com/v1beta1
   kind: IntegrationServer
   metadata:
     name: pgp-integration-server
   spec:
     version: 13.0.6.0-r1
     license:
       accept: true
       license: L-MJTK-WUU8HE
     configurations:
     - pgp-supportpac-config
   ```

2. **Create configuration for PGP libraries:**
   ```yaml
   apiVersion: v1
   kind: Configuration
   metadata:
     name: pgp-supportpac-config
   spec:
     type: generic
     data:
       # Base64 encoded JAR files
   ```

### OpenShift

When deploying to OpenShift:

1. **Security Context Constraints:**
   ```yaml
   securityContext:
     runAsUser: 1000
     fsGroup: 1000
     runAsNonRoot: true
   ```

2. **Persistent Volume for shared-classes:**
   ```yaml
   volumeMounts:
   - name: shared-classes
     mountPath: /home/aceuser/ace-server/shared-classes
   volumes:
   - name: shared-classes
     persistentVolumeClaim:
       claimName: ace-shared-classes-pvc
   ```

---

## Migration from BC 1.78.1 to 1.81

If you're upgrading from an existing deployment:

### Step 1: Backup Current Deployment

```bash
# Backup current JARs
kubectl cp <pod-name>:/opt/ibm/ace-13/server/jplugin/PGPSupportPacImpl.jar ./backup/
kubectl cp <pod-name>:/home/aceuser/ace-server/shared-classes/bcpg-jdk18on-1.78.1.jar ./backup/
```

### Step 2: Update Deployment

```bash
# Update ConfigMap or rebuild container image with BC 1.81 files
kubectl apply -f pgp-supportpac-1.81-configmap.yaml

# Rolling update
kubectl rollout restart deployment/ace-integration-server
```

### Step 3: Verify Upgrade

```bash
# Check BC version
kubectl exec <pod-name> -- unzip -p /home/aceuser/ace-server/shared-classes/bcpg-jdk18on-1.81.jar META-INF/MANIFEST.MF | grep Implementation-Version

# Run tests
kubectl exec <pod-name> -- curl -X POST http://localhost:7800/pgp/encrypt
```

### Step 4: Rollback Plan (if needed)

```bash
# Restore from backup
kubectl cp ./backup/PGPSupportPacImpl.jar <pod-name>:/opt/ibm/ace-13/server/jplugin/
kubectl cp ./backup/bcpg-jdk18on-1.78.1.jar <pod-name>:/home/aceuser/ace-server/shared-classes/

# Restart Integration Server
kubectl exec <pod-name> -- mqsistop
kubectl exec <pod-name> -- mqsistart
```

---

## Testing in Container

### Create Test Message Flow

```xml
<!-- Simple test flow -->
<ecore:EPackage xmi:version="2.0" ...>
  <nodes xmi:type="ComIbmWSInput.msgnode" ...>
    <translation xmi:type="utility:ConstantString" string="/pgp/test"/>
  </nodes>
  <nodes xmi:type="PGPEncrypter.msgnode" ...>
    <!-- PGP encryption configuration -->
  </nodes>
  <nodes xmi:type="ComIbmWSReply.msgnode" .../>
</ecore:EPackage>
```

### Deploy and Test

```bash
# Deploy BAR file
mqsideploy -i default -a PGPTest.bar

# Test encryption
curl -X POST http://<service-url>/pgp/test \
  -H "Content-Type: text/plain" \
  -d "Test message"

# Expected: PGP encrypted output
```

---

## Performance Considerations

### Resource Limits

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "2000m"
```

### JVM Tuning

```yaml
env:
- name: JAVA_OPTS
  value: "-Xms512m -Xmx1024m -XX:+UseG1GC"
```

### Bouncy Castle Provider Registration

The PGP SupportPac automatically registers BC provider. No additional configuration needed.

---

## Support and Troubleshooting

### Logs to Check

1. **Integration Server logs:**
   ```bash
   /home/aceuser/ace-server/logs/integration_server.txt
   ```

2. **Container logs:**
   ```bash
   kubectl logs <pod-name>
   ```

3. **Event logs:**
   ```bash
   kubectl describe pod <pod-name>
   ```

### Common Log Messages

✅ **Success:**
```
BIP2155I: About to 'Start' the deployed resource 'PGPTest'
BIP3132I: The HTTP Listener has started listening on port '7800'
```

❌ **Failure:**
```
BIP2230E: Error detected whilst processing a message in node 'PGPEncrypter'
java.lang.ClassNotFoundException: org.bouncycastle.openpgp.PGPPublicKey
```

---

## Conclusion

This guide provides comprehensive instructions for deploying the PGP SupportPac to IBM ACE containers with Bouncy Castle 1.81. The key points are:

1. ✅ Match BC versions (container = 1.81, plugin = 1.81)
2. ✅ Deploy `bcpg-jdk18on-1.81.jar` to shared-classes
3. ✅ Verify deployment with tests
4. ✅ Monitor for version mismatches

For questions or issues, refer to the troubleshooting section or check the project documentation.

---

**Document Version:** 1.0  
**Last Updated:** 2026-02-13  
**Tested With:** IBM ACE 13.0.6.0 Container Images  
**Bouncy Castle Version:** 1.81