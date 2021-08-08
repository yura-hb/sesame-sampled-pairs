import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Properties;

class PropertyCacheFile {
    /**
     * Load cached values from file.
     * @throws IOException when there is a problems with file read
     */
    public void load() throws IOException {
	// get the current config so if the file isn't found
	// the first time the hash will be added to output file
	configHash = getHashCodeBasedOnObjectContent(config);
	final File file = new File(fileName);
	if (file.exists()) {
	    try (InputStream inStream = Files.newInputStream(file.toPath())) {
		details.load(inStream);
		final String cachedConfigHash = details.getProperty(CONFIG_HASH_KEY);
		if (!configHash.equals(cachedConfigHash)) {
		    // Detected configuration change - clear cache
		    reset();
		}
	    }
	} else {
	    // put the hash in the file if the file is going to be created
	    reset();
	}
    }

    /** Generated configuration hash. **/
    private String configHash;
    /** Configuration object. **/
    private final Configuration config;
    /** File name of cache. **/
    private final String fileName;
    /** The details on files. **/
    private final Properties details = new Properties();
    /**
     * The property key to use for storing the hashcode of the
     * configuration. To avoid name clashes with the files that are
     * checked the key is chosen in such a way that it cannot be a
     * valid file name.
     */
    public static final String CONFIG_HASH_KEY = "configuration*?";

    /**
     * Calculates the hashcode for the serializable object based on its content.
     * @param object serializable object.
     * @return the hashcode for serializable object.
     */
    private static String getHashCodeBasedOnObjectContent(Serializable object) {
	try {
	    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    // in-memory serialization of Configuration
	    serialize(object, outputStream);
	    // Instead of hexEncoding outputStream.toByteArray() directly we
	    // use a message digest here to keep the length of the
	    // hashcode reasonable

	    final MessageDigest digest = MessageDigest.getInstance("SHA-1");
	    digest.update(outputStream.toByteArray());

	    return BaseEncoding.base16().upperCase().encode(digest.digest());
	} catch (final IOException | NoSuchAlgorithmException ex) {
	    // rethrow as unchecked exception
	    throw new IllegalStateException("Unable to calculate hashcode.", ex);
	}
    }

    /**
     * Resets the cache to be empty except for the configuration hash.
     */
    public void reset() {
	details.clear();
	details.setProperty(CONFIG_HASH_KEY, configHash);
    }

    /**
     * Serializes object to output stream.
     * @param object object to be serialized
     * @param outputStream serialization stream
     * @throws IOException if an error occurs
     */
    private static void serialize(Serializable object, OutputStream outputStream) throws IOException {
	final ObjectOutputStream oos = new ObjectOutputStream(outputStream);
	try {
	    oos.writeObject(object);
	} finally {
	    flushAndCloseOutStream(oos);
	}
    }

    /**
     * Flushes and closes output stream.
     * @param stream the output stream
     * @throws IOException  when there is a problems with file flush and close
     */
    private static void flushAndCloseOutStream(OutputStream stream) throws IOException {
	if (stream != null) {
	    Flushables.flush(stream, false);
	}
	Closeables.close(stream, false);
    }

}

