import java.util.Arrays;

class ArgTokenizer {
    /**
     * Parses the next token of this tokenizer.
     */
    public void nextToken() {
	byte ct[] = ctype;
	int c;
	int lctype;
	sval = null;
	isQuoted = false;

	do {
	    c = read();
	    if (c &lt; 0) {
		return;
	    }
	    lctype = (c &lt; 256) ? ct[c] : unicode2ctype(c);
	} while (lctype == CT_WHITESPACE);

	if (lctype == CT_ALPHA) {
	    int i = 0;
	    do {
		if (i &gt;= buf.length) {
		    buf = Arrays.copyOf(buf, buf.length * 2);
		}
		buf[i++] = (char) c;
		c = read();
		lctype = c &lt; 0 ? CT_WHITESPACE : (c &lt; 256) ? ct[c] : unicode2ctype(c);
	    } while (lctype == CT_ALPHA);
	    if (c &gt;= 0)
		--next; // push last back
	    sval = String.copyValueOf(buf, 0, i);
	    return;
	}

	if (lctype == CT_QUOTE) {
	    int quote = c;
	    int i = 0;
	    /* Invariants (because \Octal needs a lookahead):
	     *   (i)  c contains char value
	     *   (ii) d contains the lookahead
	     */
	    int d = read();
	    while (d &gt;= 0 && d != quote) {
		if (d == '\\') {
		    c = read();
		    int first = c; /* To allow \377, but not \477 */
		    if (c &gt;= '0' && c &lt;= '7') {
			c = c - '0';
			int c2 = read();
			if ('0' &lt;= c2 && c2 &lt;= '7') {
			    c = (c &lt;&lt; 3) + (c2 - '0');
			    c2 = read();
			    if ('0' &lt;= c2 && c2 &lt;= '7' && first &lt;= '3') {
				c = (c &lt;&lt; 3) + (c2 - '0');
				d = read();
			    } else
				d = c2;
			} else
			    d = c2;
		    } else {
			switch (c) {
			case 'a':
			    c = 0x7;
			    break;
			case 'b':
			    c = '\b';
			    break;
			case 'f':
			    c = 0xC;
			    break;
			case 'n':
			    c = '\n';
			    break;
			case 'r':
			    c = '\r';
			    break;
			case 't':
			    c = '\t';
			    break;
			case 'v':
			    c = 0xB;
			    break;
			}
			d = read();
		    }
		} else {
		    c = d;
		    d = read();
		}
		if (i &gt;= buf.length) {
		    buf = Arrays.copyOf(buf, buf.length * 2);
		}
		buf[i++] = (char) c;
	    }

	    if (d == quote) {
		isQuoted = true;
	    }
	    sval = String.copyValueOf(buf, 0, i);
	}
    }

    private final byte ctype[] = new byte[256];
    private String sval;
    private boolean isQuoted = false;
    private static final byte CT_WHITESPACE = 1;
    private static final byte CT_ALPHA = 0;
    private char buf[] = new char[20];
    private int next = 0;
    private static final byte CT_QUOTE = 8;
    private final int length;
    private final String str;

    /**
     * Reads a single character.
     *
     * @return The character read, or -1 if the end of the stream has been
     * reached
     */
    private int read() {
	if (next &gt;= length) {
	    return -1;
	}
	return str.charAt(next++);
    }

    private int unicode2ctype(int c) {
	switch (c) {
	case 0x1680:
	case 0x180E:
	case 0x200A:
	case 0x202F:
	case 0x205F:
	case 0x3000:
	    return CT_WHITESPACE;
	default:
	    return CT_ALPHA;
	}
    }

}

