import java.io.Reader;
import java.util.Queue;

class LineReader {
    /**
    * Reads a line of text. A line is considered to be terminated by any one of a line feed ({@code
    * '\n'}), a carriage return ({@code '\r'}), or a carriage return followed immediately by a
    * linefeed ({@code "\r\n"}).
    *
    * @return a {@code String} containing the contents of the line, not including any
    *     line-termination characters, or {@code null} if the end of the stream has been reached.
    * @throws IOException if an I/O error occurs
    */
    @CanIgnoreReturnValue // to skip a line
    public String readLine() throws IOException {
	while (lines.peek() == null) {
	    cbuf.clear();
	    // The default implementation of Reader#read(CharBuffer) allocates a
	    // temporary char[], so we call Reader#read(char[], int, int) instead.
	    int read = (reader != null) ? reader.read(buf, 0, buf.length) : readable.read(cbuf);
	    if (read == -1) {
		lineBuf.finish();
		break;
	    }
	    lineBuf.add(buf, 0, read);
	}
	return lines.poll();
    }

    private final Queue&lt;String&gt; lines = new LinkedList&lt;&gt;();
    private final CharBuffer cbuf = createBuffer();
    private final @Nullable Reader reader;
    private final char[] buf = cbuf.array();
    private final Readable readable;
    private final LineBuffer lineBuf = new LineBuffer() {
	@Override
	protected void handleLine(String line, String end) {
	    lines.add(line);
	}
    };

}

