class StringUtils {
    class TraditionalBinaryPrefix extends Enum&lt;TraditionalBinaryPrefix&gt; {
	/**
	 * @return The TraditionalBinaryPrefix object corresponding to the symbol.
	 */
	public static TraditionalBinaryPrefix valueOf(char symbol) {
	    symbol = Character.toUpperCase(symbol);
	    for (TraditionalBinaryPrefix prefix : TraditionalBinaryPrefix.values()) {
		if (symbol == prefix.symbol) {
		    return prefix;
		}
	    }
	    throw new IllegalArgumentException("Unknown symbol '" + symbol + "'");
	}

	public final char symbol;

    }

}

