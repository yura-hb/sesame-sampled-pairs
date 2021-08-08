import java.io.InputStream;

class JavaUtils {
    /**
     * This method reads all bytes from the given InputStream till EOF and
     * returns them as a byte array.
     *
     * @param inputStream
     * @return the bytes read from the stream
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
	try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
	    byte buf[] = new byte[4 * 1024];
	    int len;
	    while ((len = inputStream.read(buf)) &gt; 0) {
		baos.write(buf, 0, len);
	    }
	    return baos.toByteArray();
	}
    }

}

