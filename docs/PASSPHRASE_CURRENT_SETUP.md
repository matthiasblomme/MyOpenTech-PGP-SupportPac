# Current Passphrase Setup

This document describes how PGP key passphrases are currently handled in the PGPEncrypterNode and PGPDecrypterNode, and defines the design for ACE vault integration.

---

## Source 1: UserDefined Policy (Configurable Service)

On first message (lazy init), the node calls `getPolicy("UserDefined", getPgpPolicy())` to load the named policy. From it, it reads:

- **Decrypter:** `DefaultDecryptionKeyPassphrase` — stored as plain text in the policy
- **Encrypter:** `DefaultSignKeyPassphrase` and `DefaultSignKeyUserId` — stored as plain text in the policy

These values are cached in instance fields for the lifetime of the node:

```java
// PGPDecrypterNode
private String defaultDecryptionKeyPassphrase = null;

// PGPEncrypterNode
private String defaultSignKeyUserId = "";
private String defaultSignKeyPassphrase = null;
```

---

## Source 2: Node Property (set in ACE Toolkit)

- **Decrypter:** `decryptionKeyPassphrase` — plain text string property on the node
- **Encrypter:** `signKeyPassphrase` — plain text string property on the node

---

## The Switch Between Sources

A toggle property on each node controls which source is used:

- **Decrypter:** `useDefaultDecryptionKeyPassphrase` (Yes/No)
  - `Yes` → use the policy value (`defaultDecryptionKeyPassphrase`)
  - `No` → use the node property (`decryptionKeyPassphrase`)

- **Encrypter:** `useDefaultSignKey` (Yes/No)
  - `Yes` → use both `defaultSignKeyUserId` and `defaultSignKeyPassphrase` from policy
  - `No` → use node properties `signKeyUserId` and `signKeyPassphrase`

---

## Source 3: Local Environment (per-message override)

Both nodes allow overriding the passphrase on a per-message basis via the Local Environment message tree:

- **Decrypter:** `LocalEnvironment/PGP/Decryption/DecryptionKeyPassphrase`
- **Encrypter:** `LocalEnvironment/PGP/Encryption/SignKeyPassphrase`

This takes highest precedence and is also plain text in the message tree.

---

## Source 4: ACE Vault ⬅ TO BE IMPLEMENTED

ACE has a built-in credential vault managed via `ibmint * vault`.
Credentials inside the vault are managed via `ibmint * credentials`.
Reference: https://www.ibm.com/docs/en/app-connect/13.0.x?topic=vault-configuring-external-directory

The idea is to allow the node to look up the passphrase from the vault at runtime using a credential name, rather than storing it as plain text. Access to the vault is done via Java code:
https://www.ibm.com/docs/en/app-connect/13.0.x?topic=java-accessing-credentials-from-user-code

By accessing the vault in the proper way, credentials can be rotated live via `ibmint` without redeploying or reloading the application.

> **Credential type: `userdefined`**
> The `userdefined` credential type is used, storing the passphrase in its `password` field. Credentials are registered via:
> ```
> ibmint create credentials --credential-type userdefined --credential-name <name> --password <passphrase>
> ```

---

### Java API — Pattern 3 (MbCredential with update/delete handling)

The implementation uses **Pattern 3** from the ACE Java API docs: obtain the credential once, cache it, and check `hasBeenUpdated()` on each use so that credential rotations (via `ibmint`) take effect live without a restart.

```java
import com.ibm.broker.plugin.MbCredential;
import com.ibm.broker.plugin.MbCredentialDeletedException;
import com.ibm.broker.plugin.MbCredentialUpdatedException;

// --- Initial load (e.g. in evaluate() on first use, or in a lazy-init block) ---
MbCredential credential = MbCredential.getCredential("userdefined", credentialName);
String passphrase = (String) credential.getField("password");

// --- On every subsequent message (Pattern 3: check for live rotation) ---
if (credential.hasBeenUpdated()) {
    try {
        credential.reload();
        passphrase = (String) credential.getField("password");
    } catch (MbCredentialDeletedException e) {
        throw new MbException(
            getClass().getName(), "credentialDeleted", "",
            new Object[]{ credentialName }, e);
    }
}
```

Key points:
- `MbCredential.getCredential(type, name)` throws `MbException` if the credential does not exist — treat as a configuration error.
- `hasBeenUpdated()` is cheap; call it on every message to pick up live rotations.
- `reload()` throws `MbCredentialDeletedException` if the credential was removed after the initial load.
- The `passphrase` string retrieved from `"password"` is used exactly like the existing plain-text passphrase — no other code path changes.

---

### New Node Properties (to be added to both nodes)

Grouped under an **ACE Credentials** property group in the Toolkit UI to keep display names concise and clearly separated from the existing passphrase properties.

| Property (internal name)                           | Display label                                     | Type            | Default |
|----------------------------------------------------|---------------------------------------------------|-----------------|---------|
| `useAceCredentials`                                | Use ACE Credentials                               | Boolean (Yes/No)| No      |
| `aceCredentialPassphraseName`                      | Passphrase credential name                        | String          | —       |
| `aceCredentialDefaultDecryptionKeyPassphraseName`  | Default decryption key passphrase credential name | String          | —       |
| `aceCredentialDefaultSignKeyPassphraseName`        | Default sign key passphrase credential name       | String          | —       |

When `useAceCredentials = Yes`, these names are used to look up passphrases from the vault. The existing plain-text passphrase properties remain on the node but are ignored.

---

### New Policy Properties (to be added to the UserDefined policy)

| Property name                                      | Description                                                      |
|----------------------------------------------------|------------------------------------------------------------------|
| `AceCredentialDefaultDecryptionKeyPassphraseName`  | Vault credential name for the default decryption key passphrase  |
| `AceCredentialDefaultSignKeyPassphraseName`        | Vault credential name for the default sign key passphrase        |

---

### Local Environment Extensions (when `useAceCredentials = Yes`)

When vault mode is active, the local environment can still override passphrases on a per-message basis — but only by specifying a **credential name**, not the passphrase itself.

**Decrypter:**
- `LocalEnvironment/PGP/Decryption/AceCredentials/PassphraseName`
- `LocalEnvironment/PGP/Decryption/AceCredentials/DefaultDecryptionKeyPassphraseName`

**Encrypter:**
- `LocalEnvironment/PGP/Encryption/AceCredentials/PassphraseName`
- `LocalEnvironment/PGP/Encryption/AceCredentials/DefaultSignKeyPassphraseName`

---

### Behaviour

- All existing sources (node property, policy, local environment plain-text passphrases) continue to work unchanged when `useAceCredentials = No`.
- When `useAceCredentials = Yes`, all passphrase resolution goes through the vault — no plain-text passphrases are read from node properties or policy.
- The local environment credential name overrides (above) apply per-message even in vault mode, allowing per-message key selection without exposing passphrases in the message tree.

---

## Priority Order (highest to lowest)

Two modes depending on `useAceCredentials`:

### When `useAceCredentials = No` (existing behaviour, unchanged)

| Priority | Source                                            |
|----------|---------------------------------------------------|
| 1        | Local Environment — plain passphrase              |
| 2        | Node property — plain passphrase                  |
| 3        | Policy / Configurable Service — plain passphrase  |

### When `useAceCredentials = Yes` (new vault mode)

| Priority | Source                                                |
|----------|-------------------------------------------------------|
| 1        | Local Environment — credential name → vault lookup    |
| 2        | Node property — credential name → vault lookup        |
| 3        | Policy — credential name property → vault lookup      |

> **Lookup hierarchy:** local environment → node → policy. Same as the existing plain-text hierarchy — unchanged by vault mode.

---

## Problem (addressed by Source 4)

In all three existing paths the passphrase is a plain `String` — it is visible in:
- The ACE Configurable Service (policy file on disk)
- The flow/node properties in the ACE Toolkit
- The message tree at runtime

There is no integration with ACE's built-in credential vault (`ibmint`/`mqsisetdbparms`) or any external secret store.

---

## Related Files

- [`PGPDecrypterNode.java`](../src/ACEv13/v2.0.1.0/PGPSupportPacImpl/src/com/ibm/broker/supportpac/pgp/impl/PGPDecrypterNode.java)
- [`PGPEncrypterNode.java`](../src/ACEv13/v2.0.1.0/PGPSupportPacImpl/src/com/ibm/broker/supportpac/pgp/impl/PGPEncrypterNode.java)
