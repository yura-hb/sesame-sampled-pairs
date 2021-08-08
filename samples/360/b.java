import java.io.ByteArrayOutputStream;
import java.io.InputStream;

abstract class DataTransferer {
    /**
     * Helper function to convert an InputStream to a byte[] array.
     */
    protected static byte[] inputStreamToByteArray(InputStream str) throws IOException {
	try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
	    int len = 0;
	    byte[] buf = new byte[8192];

	    while ((len = str.read(buf)) != -1) {
		baos.write(buf, 0, len);
	    }

	    return baos.toByteArray();
	}
    }

}

