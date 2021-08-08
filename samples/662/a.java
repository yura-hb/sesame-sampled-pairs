import java.nio.charset.Charset;

class StringUtils {
    /**
     * Converts a &lt;code&gt;byte[]&lt;/code&gt; to a String using the specified character encoding.
     *
     * @param bytes
     *            the byte array to read from
     * @param charset
     *            the encoding to use, if null then use the platform default
     * @return a new String
     * @throws NullPointerException
     *             if {@code bytes} is null
     * @since 3.2
     * @since 3.3 No longer throws {@link UnsupportedEncodingException}.
     */
    public static String toEncodedString(final byte[] bytes, final Charset charset) {
	return new String(bytes, charset != null ? charset : Charset.defaultCharset());
    }

}

