import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

class Text extends BinaryComparable implements WritableComparable&lt;BinaryComparable&gt; {
    /**
     * Converts the provided byte array to a String using the
     * UTF-8 encoding. If &lt;code&gt;replace&lt;/code&gt; is true, then
     * malformed input is replaced with the
     * substitution character, which is U+FFFD. Otherwise the
     * method throws a MalformedInputException.
     */
    public static String decode(byte[] utf8, int start, int length, boolean replace) throws CharacterCodingException {
	return decode(ByteBuffer.wrap(utf8, start, length), replace);
    }

    private static ThreadLocal&lt;CharsetDecoder&gt; DECODER_FACTORY = new ThreadLocal&lt;CharsetDecoder&gt;() {
	protected CharsetDecoder initialValue() {
	    return Charset.forName("UTF-8").newDecoder().onMalformedInput(CodingErrorAction.REPORT)
		    .onUnmappableCharacter(CodingErrorAction.REPORT);
	}
    };

    private static String decode(ByteBuffer utf8, boolean replace) throws CharacterCodingException {
	CharsetDecoder decoder = DECODER_FACTORY.get();
	if (replace) {
	    decoder.onMalformedInput(java.nio.charset.CodingErrorAction.REPLACE);
	    decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
	}
	String str = decoder.decode(utf8).toString();
	// set decoder back to its default value: REPORT
	if (replace) {
	    decoder.onMalformedInput(CodingErrorAction.REPORT);
	    decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
	}
	return str;
    }

}

