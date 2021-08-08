import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

abstract class AbstractPathTestSupport {
    /** Reads the contents of a file.
     * @param filename the name of the file whose contents are to be read
     * @return contents of the file with all {@code \r\n} replaced by {@code \n}
     * @throws IOException if I/O exception occurs while reading
     */
    protected static String readFile(String filename) throws IOException {
	return new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8).replaceAll(CRLF_REGEX,
		LF_REGEX);
    }

    protected static final String CRLF_REGEX = "\\\\r\\\\n";
    protected static final String LF_REGEX = "\\\\n";

}

