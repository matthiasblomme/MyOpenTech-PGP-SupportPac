package com.ibm.broker.supportpac.pgp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.security.SignatureException;
import java.util.Iterator;

import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;

/**
 * Provides PGP decryption and signature validation functionality using Bouncy Castle library.
 *
 * <p>This class handles decryption of PGP-encrypted data and validates digital signatures.
 * It supports both text and stream-based decryption operations with configurable key repositories.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Decrypt PGP-encrypted text and binary data</li>
 *   <li>Validate PGP signatures</li>
 *   <li>Support for multiple key repositories</li>
 *   <li>Automatic key selection based on encrypted data</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Decrypt UTF-8 text
 * String encryptedText = "-----BEGIN PGP MESSAGE-----...";
 * PGPDecryptionResult result = PGPDecrypter.decryptUTF8Text(encryptedText, "passphrase");
 * String plainText = result.getDecryptedText();
 *
 * // Decrypt stream with custom key repository
 * try (InputStream in = new FileInputStream("encrypted.pgp");
 *      OutputStream out = new FileOutputStream("decrypted.txt")) {
 *     PGPDecryptionResult result = PGPDecrypter.decrypt(in, out, "passphrase", "myKeyRepo");
 *     if (result.isSignatureValid()) {
 *         System.out.println("Signature verified!");
 *     }
 * }
 * }</pre>
 *
 * @version 2.0.1.0
 * @author Dipak K Pal (IBM)
 * @since 1.0
 */
public class PGPDecrypter {

	/**
	 * Decrypts PGP-encrypted UTF-8 text using the default key repository.
	 *
	 * <p>This method decrypts text that has been encrypted with PGP and returns the result
	 * along with signature validation information if the message was signed.</p>
	 *
	 * @param cipherText the PGP-encrypted text to decrypt (must not be null or empty)
	 * @param passPhrase the passphrase for the private key used for decryption (use empty string if no passphrase)
	 * @return a {@link PGPDecryptionResult} containing the decrypted text and signature validation status
	 * @throws PGPException if decryption fails, the cipherText is null/empty, or the key repository is not configured
	 */
    public static PGPDecryptionResult decryptUTF8Text(String cipherText, String passPhrase) throws PGPException {
    	if (cipherText == null || cipherText.isEmpty()) {
    		throw new PGPException("cipherText must not be null or empty");
    	}
    	PGPDecryptionResult res = null;
    	try {
    		// Get PGP Keyring
    		PGPKeyRing pgpKeyRing = PGPEnvironment.getDefaultPGPKeyRing();
			ByteArrayInputStream in = new ByteArrayInputStream(cipherText.getBytes(StandardCharsets.UTF_8));
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			if(passPhrase == null){
    			passPhrase = "";
    		}

			char[] passwd = passPhrase.toCharArray();
			res = decrypt(in, out, passwd, pgpKeyRing);
			res.setDecryptedText(out.toString(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new PGPException(e.getMessage());
		}
		return res;
    }

    /**
     * Decrypts PGP-encrypted data from an input stream using a specified key repository.
     *
     * <p>This method reads encrypted data from the input stream, decrypts it using the private key
     * from the specified repository, and writes the decrypted data to the output stream.</p>
     *
     * @param in the input stream containing PGP-encrypted data
     * @param out the output stream where decrypted data will be written
     * @param passPhrase the passphrase for the private key (use empty string if no passphrase)
     * @param pgpKeyRepositoryName the name of the key repository to use for decryption
     * @return a {@link PGPDecryptionResult} containing decryption status and signature validation information
     * @throws PGPException if decryption fails or the specified key repository cannot be found
     */
    public static PGPDecryptionResult decrypt(InputStream in, OutputStream out, String passPhrase, String pgpKeyRepositoryName)
 throws PGPException {
  
  // Input validation
  if (in == null) {
   throw new PGPException("Input stream must not be null");
  }
  if (out == null) {
   throw new PGPException("Output stream must not be null");
  }
  if (pgpKeyRepositoryName == null || pgpKeyRepositoryName.trim().isEmpty()) {
   throw new PGPException("Key repository name must not be null or empty");
  }

    	PGPDecryptionResult decryptionRes = null;

    	try {
    		// Get PGP Keyring
    		PGPKeyRing pgpKeyRing = PGPEnvironment.getPGPKeyRing(pgpKeyRepositoryName);
    		
    		if(passPhrase == null){
    			passPhrase = "";
    		}
    		
    		char[] passwd = passPhrase.toCharArray();
  decryptionRes = decrypt(in, out, passwd, pgpKeyRing);
 } catch (Exception e) {
  throw new PGPException(e.getMessage());
 }
 return decryptionRes;
    }
    
    /**
     * Decrypts PGP-encrypted data from an input stream using the default key repository.
     *
     * <p>This method reads encrypted data from the input stream, decrypts it using the private key
     * from the default repository, and writes the decrypted data to the output stream.</p>
     *
     * @param in the input stream containing PGP-encrypted data
     * @param out the output stream where decrypted data will be written
     * @param passPhrase the passphrase for the private key (use empty string if no passphrase)
     * @return a {@link PGPDecryptionResult} containing decryption status and signature validation information
     * @throws PGPException if decryption fails or the default key repository is not configured
     */
    public static PGPDecryptionResult decrypt(InputStream in, OutputStream out, String passPhrase) throws PGPException {
  
  // Input validation
  if (in == null) {
   throw new PGPException("Input stream must not be null");
  }
  if (out == null) {
   throw new PGPException("Output stream must not be null");
  }

    	PGPDecryptionResult decryptionRes = null;

    	try {
    		// Get PGP Keyring
    		PGPKeyRing pgpKeyRing = PGPEnvironment.getDefaultPGPKeyRing();
    		
    		if(passPhrase == null){
    			passPhrase = "";
    		}
    		
    		char[] passwd = passPhrase.toCharArray();
  decryptionRes = decrypt(in, out, passwd, pgpKeyRing);
 } catch (Exception e) {
  throw new PGPException(e.getMessage());
 }
 return decryptionRes;
    }

    /**
     * Internal method to decrypt PGP-encrypted data and validate signatures.
     *
     * <p>This private method performs the actual decryption work, handling compressed data,
     * literal data extraction, and signature validation if present.</p>
     *
     * @param in the input stream containing PGP-encrypted data
     * @param out the output stream where decrypted data will be written
     * @param passwd the passphrase as a character array for the private key
     * @param pgpKeyRing the key ring containing the private and public keys
     * @return a {@link PGPDecryptionResult} containing decryption status and signature validation information
     * @throws Exception if any error occurs during decryption or signature validation
     */
    private static PGPDecryptionResult decrypt(InputStream in, OutputStream out, char[] passwd, PGPKeyRing pgpKeyRing) throws Exception {
    	
    	Provider provider = PGPJavaUtil.getProvider("BC");
    	
    	PGPDecryptionResult decryptionRes = new PGPDecryptionResult();
    	
		PGPPublicKeyEncryptedData encryptedPublicKey = null;
		
		// Get decoded input stream
		in = PGPUtil.getDecoderStream(in);

		PGPObjectFactory pgpF = new JcaPGPObjectFactory(in);
		PGPEncryptedDataList pgpEncryptedDataList;
		
		Object pgpObject = null;
		try {
			pgpObject = pgpF.nextObject();
		} catch (Exception e) {
			throw new RuntimeException("Can not recognize input data. Input data might be corrupted or not encrypted correctly. Error detail: "+e.getMessage());
		}

		if (pgpObject == null){
		    throw new RuntimeException("Can not recognize input data. Input data might be corrupted or not encrypted correctly");
		}

		// First object can be a PGP marker packet.
		if (pgpObject instanceof PGPEncryptedDataList encDataList) {
		    pgpEncryptedDataList = encDataList;
		} else {
		    pgpEncryptedDataList = (PGPEncryptedDataList) pgpF.nextObject();
		}

		// Find the secret key
		Iterator<?> encObjects = pgpEncryptedDataList.getEncryptedDataObjects();
		if (!encObjects.hasNext()){
		    throw new RuntimeException("Input does not contain any encrypted data");
		}

		PGPPrivateKey pgpPrivateKey = null;
		PGPSecretKey secretKey = null;
		
		long keyID = -1L;
		String errMsg = "";
		while (encObjects.hasNext()) {
		    // Find a key that matches our private key
		    Object obj = encObjects.next();
		    if (!(obj instanceof PGPPublicKeyEncryptedData encData)){
		        continue;
		    }
		    keyID = encData.getKeyID();		    
		    secretKey = pgpKeyRing.getPrivateKeyByID(keyID);
		    
		    //If the public key id used does not match any in the private ring, try next
		    if(secretKey == null){
		    	errMsg = errMsg + "\n" + "Private key [0x" + Integer.toHexString((int)keyID).toUpperCase() + "] " +
		    			"with subkey Id [0x" + Long.toHexString(keyID).toUpperCase() + "] not found at private key repository.";
		        continue;
		    }

		    // Extract the private key from PGPKeyRing
		    try {
		    	PBESecretKeyDecryptor decryptorFactory = 
		    			new JcePBESecretKeyDecryptorBuilder(new JcaPGPDigestCalculatorProviderBuilder().setProvider(provider).build()).setProvider(provider).build(passwd);
		        pgpPrivateKey = secretKey.extractPrivateKey(decryptorFactory);
		    } catch(Exception ex) {
		    	errMsg = errMsg + "\n" + "Can not extract a suitable private key [0x" + Integer.toHexString((int)keyID).toUpperCase() + "] " +
		    			"with subkey Id [0x" + Long.toHexString(keyID).toUpperCase() + "]: Error: " +ex.getMessage();
		    	pgpPrivateKey = null;
		    }

		    if(pgpPrivateKey != null){
		        encryptedPublicKey = encData;
		        break;
		    }
		}
		
		// Throw exception is Private Key is not found
		if (pgpPrivateKey == null) {
			String message = "A suitable private key not found at Key Repository [" + pgpKeyRing.getRepositoryName() + "]. Verify the key repository and/or passphrase.";
			
			if(!"".equals(errMsg)){
				message = message + "\nError message: " + errMsg;
			}
			
		    throw new IllegalArgumentException(message);
		}

		PublicKeyDataDecryptorFactory dataDecryptorFactory = 
				new JcePublicKeyDataDecryptorFactoryBuilder().setProvider(provider).setContentProvider(provider).build(pgpPrivateKey);
		InputStream clear = encryptedPublicKey.getDataStream(dataDecryptorFactory);

		PGPObjectFactory plainFact = new JcaPGPObjectFactory(clear);

		Object message = plainFact.nextObject();
		Object sigLiteralData = null;
		PGPObjectFactory pgpFact = null;
		boolean isCompressed = false;
		
		// Check if input data is compressed
		if (message instanceof PGPCompressedData cData) {
		    pgpFact = new JcaPGPObjectFactory(cData.getDataStream());
		    message = pgpFact.nextObject();
		    if (message instanceof PGPOnePassSignatureList){
		        sigLiteralData = pgpFact.nextObject();
		    }
		    isCompressed = true;
		}

		if (message instanceof PGPLiteralData literalData) {
		    //Message is just encrypted
		    processLiteralData(literalData, out, null);
		} else if (message instanceof PGPOnePassSignatureList onePassSigList) {

			if(!isCompressed){
		       	sigLiteralData = plainFact.nextObject();
		       }

		    //Message is signed and encrypted with OnePassSignature
		    decryptionRes.setIsSigned(true);

		    PGPSignatureWrapper sigWrap = new PGPSignatureWrapper(onePassSigList.get(0));

		    PGPPublicKey pubKey = pgpKeyRing.getPublicKeyByID(sigWrap.getKeyID());
		    
		    // If PublicKey is not available, then skip Signature validation and store exception message, but continue decryption process
		    if (pubKey == null) {
		        decryptionRes.setSignatureException(
		        		new RuntimeException("Cannot find the public key [0x" + Integer.toHexString((int)sigWrap.getKeyID()).toUpperCase() +
		        				"] with subkey Id [0x" + Long.toHexString(sigWrap.getKeyID()).toUpperCase() +
		        				"] in the PublicKey Repository [" + pgpKeyRing.getRepositoryName()+ "]"));
		        
		        // Decrypt without checking signature
		        processLiteralData((PGPLiteralData) sigLiteralData, out, null);
		    } else {
		        decryptionRes.setSignee(new PGPPublicKeyWrapper(pubKey));
		        sigWrap.initVerify(pubKey, "BC");
		        processLiteralData((PGPLiteralData) sigLiteralData, out, sigWrap);
		        PGPSignatureList sigList = null;
		        if(isCompressed){
		        	sigList = (PGPSignatureList) pgpFact.nextObject();
		        } else {
		        	sigList = (PGPSignatureList) plainFact.nextObject();
		        }
		        decryptionRes.setIsSignatureValid(sigWrap.verify(sigList.get(0)));
		    }
		} else if (message instanceof PGPSignatureList messageSignatureList) {

			if(isCompressed){
				sigLiteralData = (PGPLiteralData) pgpFact.nextObject();
		       } else {
		       	sigLiteralData = (PGPLiteralData) plainFact.nextObject();
		       }

		    //Message is signed and encrypted
		    decryptionRes.setIsSigned(true);

		    PGPSignatureWrapper sigWrap = new PGPSignatureWrapper(messageSignatureList.get(0));

		    PGPPublicKey pubKey = pgpKeyRing.getPublicKeyByID(sigWrap.getKeyID());
		    
		    // If PublicKey is not available, then skip Signature validation and store exception message, but continue decryption process
		    if (pubKey == null) {
		    	decryptionRes.setSignatureException(
		        		new RuntimeException("Cannot find the public key [0x" + Integer.toHexString((int)sigWrap.getKeyID()).toUpperCase() +
		        				"] with subkey Id [0x" + Long.toHexString(sigWrap.getKeyID()).toUpperCase() +
		        				"] in the PublicKey Repository [" + pgpKeyRing.getRepositoryName()+ "]"));
		    	
		        // Decrypt without checking signature
		        processLiteralData((PGPLiteralData) sigLiteralData, out, null);
		    } else {
		        decryptionRes.setSignee(new PGPPublicKeyWrapper(pubKey));
		        sigWrap.initVerify(pubKey, "BC");
		        processLiteralData((PGPLiteralData) sigLiteralData, out, sigWrap);
		        decryptionRes.setIsSignatureValid(sigWrap.verify(null));
		    }
		} else {
		    throw new RuntimeException("Message is not a recognizable encrypted data or not supported by this decryption application. Type unknown.\n(" + message.getClass() + ")");
		}
		
		// Integrity Check
		if (encryptedPublicKey.isIntegrityProtected()){
			decryptionRes.setIntegrityProtected(true);
		    if (!encryptedPublicKey.verify()){
		    	decryptionRes.setIntegrityCheckFailure(true);
		        throw new RuntimeException("Message failed integrity check");
		    }
		}

		return decryptionRes;
    }

    /**
     * Processes PGP literal data by extracting the content and optionally updating a signature.
     *
     * <p>This internal method reads the decrypted literal data byte-by-byte, writes it to the
     * output stream, and updates the signature verification if a signature is present.</p>
     *
     * @param ld the PGP literal data containing the decrypted content
     * @param out the output stream where the literal data will be written
     * @param sig the signature wrapper for verification, or null if no signature validation is needed
     * @throws IOException if an I/O error occurs while reading or writing data
     * @throws SignatureException if an error occurs while updating the signature
     */
    private static void processLiteralData(PGPLiteralData ld, OutputStream out, PGPSignatureWrapper sig) throws IOException, SignatureException {
        InputStream unc = ld.getInputStream();
        int ch;
        if (sig == null){
            while ((ch = unc.read()) >= 0) {
                out.write(ch);
            }
        } else {
            while ((ch = unc.read()) >= 0) {
                out.write(ch);
                sig.update((byte) ch);
            }
        }
    }
}
