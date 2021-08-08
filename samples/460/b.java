import java.util.Map;

class EnvHelp {
    /**
     * Returns the server side connection timeout.
     */
    public static long getServerConnectionTimeout(Map&lt;String, ?&gt; env) {
	return getIntegerAttribute(env, SERVER_CONNECTION_TIMEOUT, 120000L, 0, Long.MAX_VALUE);
    }

    /**
     * Name of the attribute that specifies the timeout to keep a
     * server side connection after answering last client request.
     * The default value is 120000 milliseconds.
     */
    public static final String SERVER_CONNECTION_TIMEOUT = "jmx.remote.x.server.connection.timeout";

    /**
     * Get an integer-valued attribute with name &lt;code&gt;name&lt;/code&gt;
     * from &lt;code&gt;env&lt;/code&gt;.  If &lt;code&gt;env&lt;/code&gt; is null, or does
     * not contain an entry for &lt;code&gt;name&lt;/code&gt;, return
     * &lt;code&gt;defaultValue&lt;/code&gt;.  The value may be a Number, or it
     * may be a String that is parsable as a long.  It must be at
     * least &lt;code&gt;minValue&lt;/code&gt; and at most&lt;code&gt;maxValue&lt;/code&gt;.
     *
     * @throws IllegalArgumentException if &lt;code&gt;env&lt;/code&gt; contains
     * an entry for &lt;code&gt;name&lt;/code&gt; but it does not meet the
     * constraints above.
     */
    public static long getIntegerAttribute(Map&lt;String, ?&gt; env, String name, long defaultValue, long minValue,
	    long maxValue) {
	final Object o;

	if (env == null || (o = env.get(name)) == null)
	    return defaultValue;

	final long result;

	if (o instanceof Number)
	    result = ((Number) o).longValue();
	else if (o instanceof String) {
	    result = Long.parseLong((String) o);
	    /* May throw a NumberFormatException, which is an
	       IllegalArgumentException.  */
	} else {
	    final String msg = "Attribute " + name + " value must be Integer or String: " + o;
	    throw new IllegalArgumentException(msg);
	}

	if (result &lt; minValue) {
	    final String msg = "Attribute " + name + " value must be at least " + minValue + ": " + result;
	    throw new IllegalArgumentException(msg);
	}

	if (result &gt; maxValue) {
	    final String msg = "Attribute " + name + " value must be at most " + maxValue + ": " + result;
	    throw new IllegalArgumentException(msg);
	}

	return result;
    }

}

