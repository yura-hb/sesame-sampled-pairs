class Caffeine&lt;K, V&gt; {
    /** Ensures that the argument expression is true. */
    static void requireArgument(boolean expression, String template, Object... args) {
	if (!expression) {
	    throw new IllegalArgumentException(String.format(template, args));
	}
    }

}

