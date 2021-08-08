import java.nio.charset.Charset;

class CharEncoding {
    /**
     * &lt;p&gt;Returns whether the named charset is supported.&lt;/p&gt;
     *
     * &lt;p&gt;This is similar to &lt;a
     * href="http://docs.oracle.com/javase/6/docs/api/java/nio/charset/Charset.html#isSupported%28java.lang.String%29"&gt;
     * java.nio.charset.Charset.isSupported(String)&lt;/a&gt; but handles more formats&lt;/p&gt;
     *
     * @param name  the name of the requested charset; may be either a canonical name or an alias, null returns false
     * @return {@code true} if the charset is available in the current Java virtual machine
     * @deprecated Please use {@link Charset#isSupported(String)} instead, although be aware that {@code null}
     * values are not accepted by that method and an {@link IllegalCharsetNameException} may be thrown.
     */
    @Deprecated
    public static boolean isSupported(final String name) {
	if (name == null) {
	    return false;
	}
	try {
	    return Charset.isSupported(name);
	} catch (final IllegalCharsetNameException ex) {
	    return false;
	}
    }

}

