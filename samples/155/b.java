import java.util.Map;

class Resources {
    /**
     * Returns the mnemonic for a message.
     *
     * @param message the message
     *
     * @return the mnemonic &lt;code&gt;int&lt;/code&gt;
     */
    public static int getMnemonicInt(String message) {
	Integer integer = MNEMONIC_LOOKUP.get(message);
	if (integer != null) {
	    return integer.intValue();
	}
	return 0;
    }

    private static Map&lt;String, Integer&gt; MNEMONIC_LOOKUP = Collections
	    .synchronizedMap(new IdentityHashMap&lt;String, Integer&gt;());

}

