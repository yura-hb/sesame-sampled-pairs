import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

abstract class AbstractHeaderCheck extends AbstractFileSetCheck implements ExternalResourceHolder {
    /**
     * Set the charset to use for loading the header from a file.
     * @param charset the charset to use for loading the header from a file
     * @throws UnsupportedEncodingException if charset is unsupported
     */
    public void setCharset(String charset) throws UnsupportedEncodingException {
	if (!Charset.isSupported(charset)) {
	    final String message = "unsupported charset: '" + charset + "'";
	    throw new UnsupportedEncodingException(message);
	}
	this.charset = charset;
    }

    /** Name of a charset to use for loading the header from a file. */
    private String charset = System.getProperty("file.encoding", StandardCharsets.UTF_8.name());

}

