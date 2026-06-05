package com.ibm.broker.supportpac.pgp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages the PGP security environment including key repositories and default algorithm settings.
 *
 * <p>This class provides a centralized configuration and management system for PGP operations.
 * It maintains a registry of key repositories and provides access to default encryption,
 * hashing, and compression algorithms.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Initialize and manage multiple PGP key repositories</li>
 *   <li>Configure default encryption algorithms (CAST5, AES, etc.)</li>
 *   <li>Configure default hash algorithms (SHA1, SHA256, etc.)</li>
 *   <li>Configure default compression algorithms (ZIP, ZLIB, BZIP2)</li>
 *   <li>Thread-safe repository management</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Initialize a key repository
 * PGPEnvironment.initialize(
 *     "myRepo",
 *     "/path/to/secring.gpg",
 *     "/path/to/pubring.gpg",
 *     false
 * );
 *
 * // Set as default repository
 * PGPEnvironment.setDefaultKeyRepository("myRepo");
 *
 * // Get default algorithms
 * String hashAlg = PGPEnvironment.getDefaultHashAlgorithm();  // "SHA1"
 * String cipherAlg = PGPEnvironment.getDefaultCipherAlgorithm();  // "CAST5"
 * }</pre>
 *
 * @version 2.0.1.0
 * @author Dipak K Pal (IBM)
 * @since 1.0
 */
public class PGPEnvironment {

	// Default Algorithms
	private static final String defaultHashAlgorithm = "SHA1"; //Default MD5
	private static final String defaultCipherAlgorithm = "CAST5"; //Default CAST5
	private static final String defaultCompressionAlgorithm = "ZIP"; //Default ZIP
	
	// Container (Map) to hold PGP Key Ring
	@SuppressWarnings("rawtypes")
	private static Map pgpKeyringMap;
	
	// Default Key repository name
	private static String defaultKeyRepository = "";

	private static final Object lock = new Object();
	
	/**
	 * Initializes a PGP key repository with the specified private and public key files.
	 *
	 * <p>This method registers a new key repository in the environment. If a repository with
	 * the same name already exists, it will only be overwritten if the overwrite parameter is true.</p>
	 *
	 * @param pgpRepositoryName the unique name for this key repository
	 * @param pgpPrivateKeyRepository the file path to the private key ring (secring.gpg)
	 * @param pgpPublicKeyRepository the file path to the public key ring (pubring.gpg)
	 * @param overwrite if true, overwrites an existing repository with the same name; if false, keeps the existing one
	 * @throws PGPException if initialization fails or key files cannot be loaded
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void initialize(String pgpRepositoryName, String pgpPrivateKeyRepository, 
			String pgpPublicKeyRepository, boolean overwrite) throws PGPException {

		try {
			synchronized (lock) {
				
				if(pgpKeyringMap == null){
					pgpKeyringMap = Collections.synchronizedMap(new HashMap());
				}
				
				// Get Keyring from Container
				PGPKeyRing pgpKeyring = (PGPKeyRing)pgpKeyringMap.get(pgpRepositoryName);
				
				// If Keyring is not available or have been asked for overwrite, create new Keyring
				if(pgpKeyring == null || overwrite){
					
					// Validate repository files
					if(pgpPrivateKeyRepository == null || (pgpPrivateKeyRepository != null && pgpPrivateKeyRepository.trim().length() == 0)){
						throw new RuntimeException("PrivateKey repository file name can not be null. An empty file is required in case you do not require a private key.");
					}
					
					if(pgpPublicKeyRepository == null || (pgpPublicKeyRepository != null && pgpPublicKeyRepository.trim().length() == 0)){
						throw new RuntimeException("PublicKey repository file name can not be null. An empty file is required in case you do not require a public key.");
					}
					
					pgpKeyring = new PGPKeyRing(pgpRepositoryName);
					pgpKeyring.init(pgpPrivateKeyRepository, pgpPublicKeyRepository);
					pgpKeyringMap.put(pgpRepositoryName, pgpKeyring);
				}				
			}
		} catch (Exception e) {
			throw new PGPException(e.getMessage());
		}
	}
	
	/**
	 * Print Keyring
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String printKeyRing(){
		
		if(pgpKeyringMap == null){
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		Iterator iterator = pgpKeyringMap.keySet().iterator();
		
		while (iterator.hasNext()) {
			PGPKeyRing pgpKeyring = (PGPKeyRing)pgpKeyringMap.get(iterator.next());
			sb.append("\n<===================== PGP Key Repository: "+ pgpKeyring.getRepositoryName()+ " ========================>");
			sb.append("\n<===================== Private Keys =======================>\n");
			sb.append(pgpKeyring.printPrivateKeys());
			sb.append("<===================== Public Keys  =======================>\n");
			sb.append(pgpKeyring.printPublicKeys());
		}
		
		return sb.toString();
	}
	
	/**
	 * Retrieves the default PGP key ring.
	 *
	 * <p>Returns the key ring associated with the default repository. The default repository
	 * must be set using {@link #setDefaultKeyRepository(String)} before calling this method.</p>
	 *
	 * @return the default PGP key ring containing public and private keys
	 * @throws PGPException if the default repository is not initialized or cannot be found
	 */
	public static PGPKeyRing getDefaultPGPKeyRing() throws PGPException {
		PGPKeyRing pgpKeyring = null;
		try {
			pgpKeyring = findPGPKeyRing(defaultKeyRepository);
		} catch (Exception e) {
			throw new PGPException("Default PGP Keyring not initialized: " + e.getMessage());
		}
		return pgpKeyring;
	}
	
	/**
	 * Retrieves a specific PGP key ring by repository name.
	 *
	 * <p>Returns the key ring associated with the specified repository. The repository
	 * must be initialized using {@link #initialize} before calling this method.</p>
	 *
	 * @param pgpRepositoryName the name of the key repository to retrieve
	 * @return the PGP key ring containing public and private keys
	 * @throws PGPException if the repository is not initialized or cannot be found
	 */
	public static PGPKeyRing getPGPKeyRing(String pgpRepositoryName) throws PGPException {
		PGPKeyRing pgpKeyring = null;
		try {
			pgpKeyring = findPGPKeyRing(pgpRepositoryName);
		} catch (Exception e) {
			throw new PGPException(e.getMessage());
		}
		return pgpKeyring;
	}
	
	/**
	 * Return the default hash (digest) algorithm name.
	 * @return default hash algorithm (e.g. "SHA1")
	 */
	public static String getDefaultHashAlgorithm() {
		return defaultHashAlgorithm;
	}

	/**
	 * Return the default symmetric cipher algorithm name.
	 * @return default cipher algorithm (e.g. "CAST5")
	 */
	public static String getDefaultCipherAlgorithm() {
		return defaultCipherAlgorithm;
	}

	/**
	 * Return the default compression algorithm name.
	 * @return default compression algorithm (e.g. "ZIP")
	 */
	public static String getDefaultCompressionAlgorithm() {
		return defaultCompressionAlgorithm;
	}

	/**
	 * Return the name of the default key repository.
	 * @return default key repository name
	 */
	public static String getDefaultKeyRepository() {
		return defaultKeyRepository;
	}
	
	/**
	 * Sets the default PGP key repository.
	 *
	 * <p>Designates a specific repository as the default for PGP operations. The repository
	 * must already be initialized before it can be set as the default.</p>
	 *
	 * @param defaultKeyRepository the name of the repository to set as default
	 * @throws PGPException if the specified repository is not initialized or cannot be found
	 */
	public static void setDefaultKeyRepository(String defaultKeyRepository) throws PGPException {
		try {
			findPGPKeyRing(defaultKeyRepository);
		} catch (Exception e) {
			throw new PGPException(e.getMessage());
		}
		PGPEnvironment.defaultKeyRepository = defaultKeyRepository;
	}
	
	/**
	 * Find specific Key Repository
	 * @param keyRepositoryname
	 * @return PGPKeyRing
	 * @throws Exception
	 */
	private static PGPKeyRing findPGPKeyRing(String keyRepositoryname) throws Exception {
		
		// Validate whether PGP Key Repository/Container is initialized or not
		boolean invalid = false;
		if(pgpKeyringMap == null){
			invalid = true;
		}
		
		PGPKeyRing pgpKeyring = (PGPKeyRing)pgpKeyringMap.get(keyRepositoryname);
		
		if(pgpKeyring == null){
			invalid = true;
		}
		
		if(!pgpKeyring.isInitialized()){
			invalid = true;
		}
		
		if(invalid){
			throw new RuntimeException("PGP Key Repository not initialized: " + keyRepositoryname);
		}
		
		return pgpKeyring;
	}

}
