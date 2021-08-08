import java.io.InputStream;

class InputStreamUtil {
    /**
     * Count number of lines in a file
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static int countLines(InputStream is) throws IOException {
	try {
	    byte[] c = new byte[1024];
	    int count = 0;
	    int readChars = 0;
	    boolean empty = true;
	    while ((readChars = is.read(c)) != -1) {
		empty = false;
		for (int i = 0; i &lt; readChars; ++i) {
		    if (c[i] == '\n') {
			++count;
		    }
		}
	    }
	    return (count == 0 && !empty) ? 1 : count;
	} finally {
	    is.close();
	}

    }

}

