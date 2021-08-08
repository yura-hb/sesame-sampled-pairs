import java.util.ArrayList;

class DurationFormatUtils {
    /**
     * Parses a classic date format string into Tokens
     *
     * @param format  the format to parse, not null
     * @return array of Token[]
     */
    static Token[] lexx(final String format) {
	final ArrayList&lt;Token&gt; list = new ArrayList&lt;&gt;(format.length());

	boolean inLiteral = false;
	// Although the buffer is stored in a Token, the Tokens are only
	// used internally, so cannot be accessed by other threads
	StringBuilder buffer = null;
	Token previous = null;
	for (int i = 0; i &lt; format.length(); i++) {
	    final char ch = format.charAt(i);
	    if (inLiteral && ch != '\'') {
		buffer.append(ch); // buffer can't be null if inLiteral is true
		continue;
	    }
	    Object value = null;
	    switch (ch) {
	    // TODO: Need to handle escaping of '
	    case '\'':
		if (inLiteral) {
		    buffer = null;
		    inLiteral = false;
		} else {
		    buffer = new StringBuilder();
		    list.add(new Token(buffer));
		    inLiteral = true;
		}
		break;
	    case 'y':
		value = y;
		break;
	    case 'M':
		value = M;
		break;
	    case 'd':
		value = d;
		break;
	    case 'H':
		value = H;
		break;
	    case 'm':
		value = m;
		break;
	    case 's':
		value = s;
		break;
	    case 'S':
		value = S;
		break;
	    default:
		if (buffer == null) {
		    buffer = new StringBuilder();
		    list.add(new Token(buffer));
		}
		buffer.append(ch);
	    }

	    if (value != null) {
		if (previous != null && previous.getValue().equals(value)) {
		    previous.increment();
		} else {
		    final Token token = new Token(value);
		    list.add(token);
		    previous = token;
		}
		buffer = null;
	    }
	}
	if (inLiteral) { // i.e. we have not found the end of the literal
	    throw new IllegalArgumentException("Unmatched quote in format: " + format);
	}
	return list.toArray(new Token[list.size()]);
    }

    static final Object y = "y";
    static final Object M = "M";
    static final Object d = "d";
    static final Object H = "H";
    static final Object m = "m";
    static final Object s = "s";
    static final Object S = "S";

    class Token {
	static final Object y = "y";
	static final Object M = "M";
	static final Object d = "d";
	static final Object H = "H";
	static final Object m = "m";
	static final Object s = "s";
	static final Object S = "S";

	/**
	 * Wraps a token around a value. A value would be something like a 'Y'.
	 *
	 * @param value to wrap
	 */
	Token(final Object value) {
	    this.value = value;
	    this.count = 1;
	}

	/**
	 * Gets the particular value this token represents.
	 *
	 * @return Object value
	 */
	Object getValue() {
	    return value;
	}

	/**
	 * Adds another one of the value
	 */
	void increment() {
	    count++;
	}

    }

}

