package com.ibm.broker.supportpac.pgp;

/**
 * Custom exception class for PGP-related errors.
 *
 * <p>This exception is thrown when PGP operations fail, such as encryption, decryption,
 * key management, or signature validation errors. It wraps underlying exceptions and
 * provides meaningful error messages for PGP-specific failures.</p>
 *
 * <p><b>Common Scenarios:</b></p>
 * <ul>
 *   <li>Key not found in repository</li>
 *   <li>Invalid passphrase</li>
 *   <li>Corrupted encrypted data</li>
 *   <li>Signature validation failure</li>
 *   <li>Key repository initialization errors</li>
 * </ul>
 *
 * @version 2.0.1.0
 * @author Dipak K Pal (IBM)
 * @since 1.0
 */
public class PGPException extends Exception {

	private static final long serialVersionUID = 1467346734673647L;

	/**
	 * Constructs a new PGP exception with no detail message.
	 */
	public PGPException() {
	}

	/**
	 * Constructs a new PGP exception with the specified detail message.
	 *
	 * @param message the detail message explaining the reason for the exception
	 */
	public PGPException(String message) {
		super(message);
	}

	/**
	 * Constructs a new PGP exception with the specified cause.
	 *
	 * @param cause the underlying cause of this exception
	 */
	public PGPException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new PGP exception with the specified detail message and cause.
	 *
	 * @param message the detail message explaining the reason for the exception
	 * @param cause the underlying cause of this exception
	 */
	public PGPException(String message, Throwable cause) {
		super(message, cause);
	}

}
