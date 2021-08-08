import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

class PropertyCacheFile {
    /**
     * Cleans up the object and updates the cache file.
     * @throws IOException  when there is a problems with file save
     */
    public void persist() throws IOException {
	final Path path = Paths.get(fileName);
	final Path directory = path.getParent();
	if (directory != null) {
	    Files.createDirectories(directory);
	}
	OutputStream out = null;
	try {
	    out = Files.newOutputStream(path);
	    details.store(out, null);
	} finally {
	    flushAndCloseOutStream(out);
	}
    }

    /** File name of cache. **/
    private final String fileName;
    /** The details on files. **/
    private final Properties details = new Properties();

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

