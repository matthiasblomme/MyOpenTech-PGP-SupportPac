package com.ibm.broker.supportpac.pgp;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Iterator;
import java.util.UUID;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

/**
 * Common Java utility functionalities for various PGP operations.
 * @version 1.0
 * @author Dipak K Pal (IBM)
 * <br><br>
 * <b>Description:</b>
 * Common Java utility functionalities for various PGP operations.
 *
 */
public class PGPJavaUtil {
	
	/**
	 * Get Security Provider
	 * @param providerName
	 * @return
	 * @throws NoSuchProviderException
	 */
	public static Provider getProvider(String providerName) throws NoSuchProviderException {
		Provider prov = Security.getProvider(providerName);

	    if (prov == null) {
	    	throw new NoSuchProviderException("provider " + providerName + " not found.");
	    }

	    return prov;
	}

	public static byte[] compressFile(String fileName, int algorithm) throws IOException {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(algorithm);
		PGPUtil.writeFileToLiteralData(comData.open(bOut), PGPLiteralData.BINARY, new File(fileName));
		comData.close();
		return bOut.toByteArray();
	}

	/**
	 * Search a secret key ring collection for a secret key corresponding to
	 * keyID if it exists.
	 *
	 * @param pgpSec
	 *            a secret key ring collection.
	 * @param keyID
	 *            keyID we want.
	 * @param pass
	 *            passphrase to decrypt secret key with.
	 * @return
	 * @throws PGPException
	 * @throws NoSuchProviderException
	 */
	public static PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass) throws PGPException, NoSuchProviderException {
		PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

		if (pgpSecKey == null) {
			return null;
		}
		
		PGPDigestCalculatorProvider calcProv = new JcaPGPDigestCalculatorProviderBuilder().build();
	    PBESecretKeyDecryptor decryptor = new JcePBESecretKeyDecryptorBuilder(calcProv).setProvider(PGPJavaUtil.getDefaultProvider()).build(pass);

		return pgpSecKey.extractPrivateKey(decryptor);
	}
	
	/**
	 * Read Public Key
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @throws PGPException
	 */
	public static PGPPublicKey readPublicKey(String fileName) throws IOException, PGPException {
		InputStream keyIn = new BufferedInputStream(new FileInputStream(fileName));
		PGPPublicKey pubKey = readPublicKey(keyIn);
		keyIn.close();
		return pubKey;
	}	
	
	/**
	 * Read Public Key
	 * @return
	 * @throws IOException
	 * @throws PGPException
	 */
	public static PGPPublicKey readPublicKey(byte[] pgpPublicKey) throws IOException, PGPException {
		PGPPublicKey pubKey = readPublicKey(new ByteArrayInputStream(pgpPublicKey));
		return pubKey;
	}	
	

	/**
	 * A simple routine that opens a key ring file and loads the first available
	 * key suitable for encryption.
	 *
	 * @param input
	 * @return
	 * @throws IOException
	 * @throws PGPException
	 */
	public static PGPPublicKey readPublicKey(InputStream input) throws IOException, PGPException {
		PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(input), new JcaKeyFingerprintCalculator());

		Iterator<PGPPublicKeyRing> keyRingIter = pgpPub.getKeyRings();
		while (keyRingIter.hasNext()) {
			PGPPublicKeyRing keyRing = keyRingIter.next();

			Iterator<PGPPublicKey> keyIter = keyRing.getPublicKeys();
			while (keyIter.hasNext()) {
				PGPPublicKey key = keyIter.next();

				if (key.isEncryptionKey()) {
					return key;
				}
			}
		}

		throw new IllegalArgumentException("Can't find encryption key in key repository.");
	}

	/**
	 * Read Secret Key
	 * @return
	 * @throws IOException
	 * @throws PGPException
	 */
	public static PGPSecretKey readSecretKey(byte[] pgpSecretKey) throws IOException, PGPException {
		PGPSecretKey secKey = readSecretKey(new ByteArrayInputStream(pgpSecretKey));
		return secKey;
	}
	
	/**
	 * Read Secret Key
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @throws PGPException
	 */
	public static PGPSecretKey readSecretKey(String fileName) throws IOException, PGPException {
		InputStream keyIn = new BufferedInputStream(new FileInputStream(fileName));
		PGPSecretKey secKey = readSecretKey(keyIn);
		keyIn.close();
		return secKey;
	}

	/**
	 * A simple routine that opens a key ring file and loads the first available
	 * key suitable for signature generation.
	 *
	 * @param input
	 *            stream to read the secret key ring collection from.
	 * @return a secret key.
	 * @throws IOException
	 *             on a problem with using the input stream.
	 * @throws PGPException
	 *             if there is an issue parsing the input stream.
	 */
	public static PGPSecretKey readSecretKey(InputStream input) throws IOException, PGPException {
		PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(input), new JcaKeyFingerprintCalculator());

		Iterator<PGPSecretKeyRing> keyRingIter = pgpSec.getKeyRings();
		while (keyRingIter.hasNext()) {
			PGPSecretKeyRing keyRing = (PGPSecretKeyRing) keyRingIter.next();

			Iterator<PGPSecretKey> keyIter = keyRing.getSecretKeys();
			while (keyIter.hasNext()) {
				PGPSecretKey key = (PGPSecretKey) keyIter.next();

				if (key.isSigningKey()) {
					return key;
				}
			}
		}

		throw new IllegalArgumentException("Can't find signing key in key ring.");
	}

	/**
	 * Write data to a randomly-named temporary file in the given directory.
	 * @param data the bytes to write
	 * @param tempDirectory directory in which to create the temporary file
	 * @param isEncrypted if {@code true}, appends {@code .asc} to the generated filename
	 * @return absolute path of the created file
	 * @throws IllegalArgumentException if tempDirectory is null, empty, or contains path traversal sequences
	 * @throws Exception if the file cannot be created or written
	 */
	public static String createFile(byte[] data, String tempDirectory, boolean isEncrypted) throws Exception {
		validateFilePath(tempDirectory, "tempDirectory");

		String dataFile = tempDirectory + "/" + getRandomFileName();

		if(isEncrypted){
			dataFile = dataFile + ".asc";
		}

		try (FileOutputStream fos = new FileOutputStream(new File(dataFile))) {
			fos.write(data);
		}

		return dataFile;
	}
	
	/**
	 * Write data to the specified file, optionally ASCII-armoring it.
	 * @param data the bytes to write
	 * @param fileName absolute path of the output file
	 * @param asciiAromor if {@code true}, wraps output in an ASCII armor stream
	 * @throws IllegalArgumentException if fileName is null, empty, or contains path traversal sequences
	 * @throws Exception if the file cannot be written
	 */
	public static void writeFile(byte[] data, String fileName, boolean asciiAromor) throws Exception {
		validateFilePath(fileName, "fileName");
	       File outFile = new File(fileName);
	       
	       try (FileOutputStream fout = new FileOutputStream(outFile)) {
	           if(asciiAromor){
	               try (OutputStream ostream = new ArmoredOutputStream(fout)) {
	                   ostream.write(data);
	               }
	           } else {
	               fout.write(data);
	           }
	       }
	   }
	
	/**
	 * Encode data as ASCII armored
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] encodeAsciiArmored(byte[] data) throws Exception {

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        OutputStream ostream = new ArmoredOutputStream(bout);
        ostream.write(data);
        ostream.close();
        
        byte[] output = bout.toByteArray();
        bout.close();
        
        return output;
    }

	/**
	 * Read a file and return its contents as a byte array.
	 * @param file absolute path to the file to read
	 * @return file contents as {@code byte[]}
	 * @throws IllegalArgumentException if the path is null, empty, or contains path traversal sequences
	 * @throws Exception if the file cannot be read
	 */
	public static byte[] readFile(String file) throws Exception {
		validateFilePath(file, "file");
		try (FileInputStream fis = new FileInputStream(new File(file))) {
			return fis.readAllBytes();
		}
	}

	/**
	 * Validate a file path: checks it is non-null/non-empty and contains no path traversal sequences.
	 * @param path the file path to validate
	 * @param paramName parameter name used in the exception message
	 * @throws IllegalArgumentException if the path is null, empty, or contains {@code ..}
	 */
	private static void validateFilePath(String path, String paramName) {
		if (path == null || path.trim().isEmpty()) {
			throw new IllegalArgumentException(paramName + " must not be null or empty");
		}
		try {
			String canonical = new File(path).getCanonicalPath();
			if (canonical.contains("..")) {
				throw new IllegalArgumentException(paramName + " must not contain path traversal sequences: " + path);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(paramName + " is not a valid file path: " + path);
		}
	}

	/**
	 * Delete Temporary Files
	 * @param fileName
	 */
	public static void deleteFile(String fileName){
		File file = new File(fileName);
		file.delete();
	}

	/**
	 * Translate Compression Algorithm
	 * @param algorithm
	 * @return
	 */
	public static int getCompressionAlgorithm(String algorithm) throws Exception {
		return switch (algorithm.trim().toUpperCase()) {
			case "UNCOMPRESSED" -> CompressionAlgorithmTags.UNCOMPRESSED;
			case "ZIP"          -> CompressionAlgorithmTags.ZIP;
			case "BZIP2"        -> CompressionAlgorithmTags.BZIP2;
			case "ZLIB"         -> CompressionAlgorithmTags.ZLIB;
			default -> throw new NoSuchAlgorithmException("Compression Algorithm not supported :" + algorithm);
		};
	}

	/**
	 * Public Key Algorithm
	 * @param algorithm
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static int getPublicKeyAlgorithmTags(String algorithm) throws Exception {
		return switch (algorithm.trim().toUpperCase()) {
			case "RSA_GENERAL"    -> PublicKeyAlgorithmTags.RSA_GENERAL;
			case "RSA_ENCRYPT"    -> PublicKeyAlgorithmTags.RSA_ENCRYPT;
			case "RSA_SIGN"       -> PublicKeyAlgorithmTags.RSA_SIGN;
			case "ELGAMAL_ENCRYPT"-> PublicKeyAlgorithmTags.ELGAMAL_ENCRYPT;
			case "DSA"            -> PublicKeyAlgorithmTags.DSA;
			case "EC"             -> PublicKeyAlgorithmTags.EC;
			case "ECDH"           -> PublicKeyAlgorithmTags.ECDH;
			case "ECDSA"          -> PublicKeyAlgorithmTags.ECDSA;
			case "DIFFIE_HELLMAN" -> PublicKeyAlgorithmTags.DIFFIE_HELLMAN;
			default -> throw new NoSuchAlgorithmException("PublicKey Algorithm not supported :" + algorithm);
		};
	}

	/*
	 * Get Hash Algorithm
	 */
	public static int getHashAlgorithm(String algorithm) throws Exception {
		return switch (algorithm.trim().toUpperCase()) {
			case "MD5"        -> HashAlgorithmTags.MD5;
			case "SHA1"       -> HashAlgorithmTags.SHA1;
			case "RIPEMD160"  -> HashAlgorithmTags.RIPEMD160;
			case "DOUBLE_SHA" -> HashAlgorithmTags.DOUBLE_SHA;
			case "MD2"        -> HashAlgorithmTags.MD2;
			case "TIGER_192"  -> HashAlgorithmTags.TIGER_192;
			case "HAVAL_5_160"-> HashAlgorithmTags.HAVAL_5_160;
			case "SHA256"     -> HashAlgorithmTags.SHA256;
			case "SHA384"     -> HashAlgorithmTags.SHA384;
			case "SHA512"     -> HashAlgorithmTags.SHA512;
			case "SHA224"     -> HashAlgorithmTags.SHA224;
			default -> throw new NoSuchAlgorithmException("Hash Algorithm not supported :" + algorithm);
		};
	}

	/**
	 * Translate Cipher Algorithm
	 * @param algorithm
	 * @return
	 * @throws Exception
	 */
	public static int getCipherAlgorithm(String algorithm) throws Exception {
		return switch (algorithm.trim().toUpperCase()) {
			case "NULL"        -> PGPEncryptedData.NULL;       // Plain text or unencrypted data
			case "IDEA"        -> PGPEncryptedData.IDEA;       // IDEA [IDEA]
			case "TRIPLE_DES"  -> PGPEncryptedData.TRIPLE_DES; // Triple-DES (DES-EDE, 168 bit key derived from 192)
			case "CAST5"       -> PGPEncryptedData.CAST5;      // CAST5 (128 bit key, as per RFC 2144)
			case "BLOWFISH"    -> PGPEncryptedData.BLOWFISH;   // Blowfish (128 bit key, 16 rounds) [BLOWFISH]
			case "SAFER"       -> PGPEncryptedData.SAFER;      // SAFER-SK128 (13 rounds) [SAFER]
			case "DES"         -> PGPEncryptedData.DES;        // Reserved for DES/SK
			case "AES_128"     -> PGPEncryptedData.AES_128;    // AES with 128-bit key
			case "AES_192"     -> PGPEncryptedData.AES_192;    // AES with 192-bit key
			case "AES_256"     -> PGPEncryptedData.AES_256;    // AES with 256-bit key
			case "TWOFISH"     -> PGPEncryptedData.TWOFISH;    // Twofish
			case "CAMELLIA_128"-> PGPEncryptedData.CAMELLIA_128;
			case "CAMELLIA_192"-> PGPEncryptedData.CAMELLIA_192;
			case "CAMELLIA_256"-> PGPEncryptedData.CAMELLIA_256;
			default -> throw new NoSuchAlgorithmException("Cipher Algorithm not supported :" + algorithm);
		};
	}

	/**
	 *
	 * @return
	 */
	public static byte[] getLineSeparator(){
		String lineSeparator = System.getProperty("line.separator");
		return lineSeparator.getBytes();
	}

	/**
	 *
	 * @param data
	 * @param index
	 * @return
	 */
	public static String getLine(String data, Long index){

		String[] list = data.split("\n");
		int len = list.length;

		int cnt = 0;
		for (int i = 0; i < len; i++) {
			String line = list[i];
			line = line.replaceAll("\r", "");

			if(line != null && line.trim().length() > 0){
				cnt++;
				if(index.intValue() == cnt){
					return line;
				}
			}
		}

		return null;
	}

	/**
	 *
	 * @param data
	 * @return
	 */
	public static String formatData(String data){

		String[] list = data.split("\n");
		int len = list.length;

		StringBuilder buf = new StringBuilder();
		boolean first = true;

		for (int i = 0; i < len; i++) {
			String line = list[i];
			line = line.replaceAll("\r", "");

			if(line != null && line.trim().length() > 0){
				if(first){
					buf.append(line);
					first = false;
				} else {
					buf.append("\n");
					buf.append(line);
				}
			}
		}

		return buf.toString();
	}
	
	/**
	 * Generate Random file name
	 * @return
	 */
	public static final String getRandomFileName(){

		String uuid = UUID.randomUUID().toString();
		uuid = uuid.replaceAll("-", "");

		return uuid + ".dat";
	}

	/**
	 *
	 * @return
	 */
	public static String getNewLineCharacter(){
		return "\n";
	}
	
	/**
	 * Format with correct path separator
	 * @param filePath
	 * @return
	 */
	public static String formatFilePath(String filePath){
		
		System.out.println(filePath);
		
		if(filePath != null && filePath.length() > 0){
			filePath = filePath.replaceAll("\\\\", "/");
			filePath = filePath.replaceAll("\"", "/");
			filePath = filePath.trim();
			while(filePath.charAt(filePath.length()-1) == '/' && filePath.length() != 1){
				filePath = filePath.substring(0,filePath.length()-1);
			}
		}
		
		System.out.println(filePath);
		
		return filePath;
	}
	
	/**
	 * 
	 * @return
	 */
	public static int getDefaultHashAlgorithm(){
		return HashAlgorithmTags.SHA256;
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getDefaultProvider(){
		return "BC";
	}
	
	/**
	 * 
	 * @return
	 */
	public static int getDefaultCipherAlgorithm(){
		return PGPEncryptedData.AES_256;
	}
}