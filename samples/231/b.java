import java.util.Map;
import com.sun.org.apache.xml.internal.security.signature.XMLSignature;

class JCEMapper {
    /**
     * This method registers the default algorithms.
     */
    public static void registerDefaultAlgorithms() {
	// Digest algorithms
	algorithmsMap.put(MessageDigestAlgorithm.ALGO_ID_DIGEST_NOT_RECOMMENDED_MD5,
		new Algorithm("", "MD5", "MessageDigest"));
	algorithmsMap.put(MessageDigestAlgorithm.ALGO_ID_DIGEST_RIPEMD160,
		new Algorithm("", "RIPEMD160", "MessageDigest"));
	algorithmsMap.put(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1, new Algorithm("", "SHA-1", "MessageDigest"));
	algorithmsMap.put(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA224, new Algorithm("", "SHA-224", "MessageDigest"));
	algorithmsMap.put(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256, new Algorithm("", "SHA-256", "MessageDigest"));
	algorithmsMap.put(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA384, new Algorithm("", "SHA-384", "MessageDigest"));
	algorithmsMap.put(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA512, new Algorithm("", "SHA-512", "MessageDigest"));
	algorithmsMap.put(MessageDigestAlgorithm.ALGO_ID_DIGEST_WHIRLPOOL,
		new Algorithm("", "WHIRLPOOL", "MessageDigest"));
	algorithmsMap.put(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA3_224,
		new Algorithm("", "SHA3-224", "MessageDigest"));
	algorithmsMap.put(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA3_256,
		new Algorithm("", "SHA3-256", "MessageDigest"));
	algorithmsMap.put(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA3_384,
		new Algorithm("", "SHA3-384", "MessageDigest"));
	algorithmsMap.put(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA3_512,
		new Algorithm("", "SHA3-512", "MessageDigest"));
	// Signature algorithms
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_DSA, new Algorithm("DSA", "SHA1withDSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_DSA_SHA256,
		new Algorithm("DSA", "SHA256withDSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_NOT_RECOMMENDED_RSA_MD5,
		new Algorithm("RSA", "MD5withRSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_RIPEMD160,
		new Algorithm("RSA", "RIPEMD160withRSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1, new Algorithm("RSA", "SHA1withRSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA224,
		new Algorithm("RSA", "SHA224withRSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256,
		new Algorithm("RSA", "SHA256withRSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384,
		new Algorithm("RSA", "SHA384withRSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512,
		new Algorithm("RSA", "SHA512withRSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1_MGF1,
		new Algorithm("RSA", "SHA1withRSAandMGF1", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA224_MGF1,
		new Algorithm("RSA", "SHA224withRSAandMGF1", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1,
		new Algorithm("RSA", "SHA256withRSAandMGF1", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384_MGF1,
		new Algorithm("RSA", "SHA384withRSAandMGF1", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1,
		new Algorithm("RSA", "SHA512withRSAandMGF1", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA3_224_MGF1,
		new Algorithm("RSA", "SHA3-224withRSAandMGF1", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA3_256_MGF1,
		new Algorithm("RSA", "SHA3-256withRSAandMGF1", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA3_384_MGF1,
		new Algorithm("RSA", "SHA3-384withRSAandMGF1", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA3_512_MGF1,
		new Algorithm("RSA", "SHA3-512withRSAandMGF1", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA1, new Algorithm("EC", "SHA1withECDSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA224,
		new Algorithm("EC", "SHA224withECDSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA256,
		new Algorithm("EC", "SHA256withECDSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA384,
		new Algorithm("EC", "SHA384withECDSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA512,
		new Algorithm("EC", "SHA512withECDSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_RIPEMD160,
		new Algorithm("EC", "RIPEMD160withECDSA", "Signature"));
	algorithmsMap.put(XMLSignature.ALGO_ID_MAC_HMAC_NOT_RECOMMENDED_MD5, new Algorithm("", "HmacMD5", "Mac", 0, 0));
	algorithmsMap.put(XMLSignature.ALGO_ID_MAC_HMAC_RIPEMD160, new Algorithm("", "HMACRIPEMD160", "Mac", 0, 0));
	algorithmsMap.put(XMLSignature.ALGO_ID_MAC_HMAC_SHA1, new Algorithm("", "HmacSHA1", "Mac", 0, 0));
	algorithmsMap.put(XMLSignature.ALGO_ID_MAC_HMAC_SHA224, new Algorithm("", "HmacSHA224", "Mac", 0, 0));
	algorithmsMap.put(XMLSignature.ALGO_ID_MAC_HMAC_SHA256, new Algorithm("", "HmacSHA256", "Mac", 0, 0));
	algorithmsMap.put(XMLSignature.ALGO_ID_MAC_HMAC_SHA384, new Algorithm("", "HmacSHA384", "Mac", 0, 0));
	algorithmsMap.put(XMLSignature.ALGO_ID_MAC_HMAC_SHA512, new Algorithm("", "HmacSHA512", "Mac", 0, 0));
    }

    private static Map&lt;String, Algorithm&gt; algorithmsMap = new ConcurrentHashMap&lt;String, Algorithm&gt;();

    class Algorithm {
	private static Map&lt;String, Algorithm&gt; algorithmsMap = new ConcurrentHashMap&lt;String, Algorithm&gt;();

	public Algorithm(String requiredKey, String jceName, String algorithmClass) {
	    this(requiredKey, jceName, algorithmClass, 0, 0);
	}

	public Algorithm(String requiredKey, String jceName, String algorithmClass, int keyLength, int ivLength) {
	    this(requiredKey, jceName, algorithmClass, keyLength, ivLength, null);
	}

	public Algorithm(String requiredKey, String jceName, String algorithmClass, int keyLength, int ivLength,
		String jceProvider) {
	    this.requiredKey = requiredKey;
	    this.jceName = jceName;
	    this.algorithmClass = algorithmClass;
	    this.keyLength = keyLength;
	    this.ivLength = ivLength;
	    this.jceProvider = jceProvider;
	}

    }

}

