class JavaBinaryNames {
    /**
     * Returns true iff the given method selector is a constructor.
     */
    public static boolean isConstructor(char[] selector) {
	return selector[0] == '&lt;' && selector.length == 6; // Can only match &lt;init&gt;
    }

}

