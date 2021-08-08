import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

class JAXPTestUtilities {
    /**
     * Convert stream to ByteArrayInputStream by given character set.
     * @param charset target character set.
     * @param file a file that contains no BOM head content.
     * @return a ByteArrayInputStream contains BOM heads and bytes in original
     *         stream
     * @throws IOException I/O operation failed or unsupported character set.
     */
    public static InputStream bomStream(String charset, String file) throws IOException {
	String localCharset = charset;
	if (charset.equals("UTF-16") || charset.equals("UTF-32")) {
	    localCharset += ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ? "BE" : "LE";
	}
	if (!bom.containsKey(localCharset))
	    throw new UnsupportedCharsetException("Charset:" + localCharset);

	byte[] content = Files.readAllLines(Paths.get(file)).stream().collect(Collectors.joining())
		.getBytes(localCharset);
	byte[] head = bom.get(localCharset);
	ByteBuffer bb = ByteBuffer.allocate(content.length + head.length);
	bb.put(head);
	bb.put(content);
	return new ByteArrayInputStream(bb.array());
    }

    /**
     * BOM table for storing BOM header.
     */
    private final static Map&lt;String, byte[]&gt; bom = new HashMap&lt;&gt;();

}

