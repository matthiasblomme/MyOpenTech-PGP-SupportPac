
package com.ibm.broker.supportpac.pgp;

/**
 * Holds the result of a PGP decryption operation.
 * <p>
 * In addition to the decrypted content (available via the output stream passed to
 * {@link PGPDecrypter#decrypt}), this object carries signature validation status,
 * integrity-check status, and any exception that occurred during signature verification.
 * </p>
 * @version 1.0
 * @author Dipak K Pal
 */
public class PGPDecryptionResult {

    private String decryptFileName = "";
    private boolean isSigned = false;
    private PGPPublicKeyWrapper signee = null;
    private boolean isSignatureValid = false;
    private Exception signatureException = null;
    private String decryptedText;
    private boolean isIntegrityProtected = false;
    private boolean integrityCheckFailure = false;

    /**
     * Return the name of the decrypted output file, if file-based decryption was used.
     * @return output file name, or empty string if not applicable
     */
    public String getDecryptFileName() {
        return decryptFileName;
    }

    /**
     * Set the name of the decrypted output file.
     * @param decryptFileName output file name
     */
    public void setDecryptFileName(String decryptFileName) {
        this.decryptFileName = decryptFileName;
    }

    /**
     * Return whether the decrypted message contained a PGP signature.
     * @return {@code true} if a signature was present
     */
    public boolean isIsSigned() {
        return isSigned;
    }

    /**
     * Set whether the decrypted message contained a PGP signature.
     * @param isSigned {@code true} if a signature was present
     */
    public void setIsSigned(boolean isSigned) {
        this.isSigned = isSigned;
    }

    /**
     * Return the public key wrapper for the signer, if the signature was validated.
     * @return signer's {@link PGPPublicKeyWrapper}, or {@code null} if not signed or key not found
     */
    public PGPPublicKeyWrapper getSignee() {
        return signee;
    }

    /**
     * Set the public key wrapper for the signer.
     * @param signee the signer's public key wrapper
     */
    public void setSignee(PGPPublicKeyWrapper signee) {
        this.signee = signee;
    }

    /**
     * Return whether the signature was cryptographically valid.
     * @return {@code true} if the signature verified successfully
     */
    public boolean isIsSignatureValid() {
        return isSignatureValid;
    }

    /**
     * Set whether the signature was cryptographically valid.
     * @param isSignatureValid {@code true} if the signature verified successfully
     */
    public void setIsSignatureValid(boolean isSignatureValid) {
        this.isSignatureValid = isSignatureValid;
    }

    /**
     * Return any exception that occurred during signature verification.
     * A non-null value here does not necessarily mean decryption failed — the message
     * may still have been decrypted even if the signing public key was not found.
     * @return the signature verification exception, or {@code null} if none
     */
    public Exception getSignatureException() {
        return signatureException;
    }

    /**
     * Set the exception that occurred during signature verification.
     * @param signatureException the exception, or {@code null} to clear
     */
    public void setSignatureException(Exception signatureException) {
        this.signatureException = signatureException;
    }

    /**
     * Return the decrypted content as a UTF-8 string.
     * Only populated when {@link PGPDecrypter#decryptUTF8Text} is used.
     * @return decrypted text, or {@code null} if stream-based decryption was used
     */
    public String getDecryptedText() {
        return decryptedText;
    }

    /**
     * Set the decrypted content as a string.
     * @param decryptedText decrypted text
     */
    public void setDecryptedText(String decryptedText) {
        this.decryptedText = decryptedText;
    }

    /** @return {@code true} if the message contained a PGP signature */
    public boolean isSigned() {
        return isSigned;
    }

    /** @param isSigned {@code true} if the message contained a PGP signature */
    public void setSigned(boolean isSigned) {
        this.isSigned = isSigned;
    }

    /** @return {@code true} if the signature verified successfully */
    public boolean isSignatureValid() {
        return isSignatureValid;
    }

    /** @param isSignatureValid {@code true} if the signature verified successfully */
    public void setSignatureValid(boolean isSignatureValid) {
        this.isSignatureValid = isSignatureValid;
    }

    /**
     * Return whether the message was protected by a PGP integrity packet (MDC).
     * @return {@code true} if an integrity packet was present
     */
    public boolean isIntegrityProtected() {
        return isIntegrityProtected;
    }

    /**
     * Set whether the message was protected by a PGP integrity packet.
     * @param isIntegrityProtected {@code true} if an integrity packet was present
     */
    public void setIntegrityProtected(boolean isIntegrityProtected) {
        this.isIntegrityProtected = isIntegrityProtected;
    }

    /**
     * Return whether the integrity check failed.
     * @return {@code true} if an integrity packet was present but verification failed
     */
    public boolean isIntegrityCheckFailure() {
        return integrityCheckFailure;
    }

    /**
     * Set whether the integrity check failed.
     * @param integrityCheckFailure {@code true} if integrity verification failed
     */
    public void setIntegrityCheckFailure(boolean integrityCheckFailure) {
        this.integrityCheckFailure = integrityCheckFailure;
    }

}
