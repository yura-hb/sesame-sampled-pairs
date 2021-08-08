class CharArrayUtils {
    /**
     * Answers a new array which is the concatenation of all the given arrays.
     *
     * @param toCatenate
     * @since 3.12
     */
    public static char[] concat(char[]... toCatenate) {
	int totalSize = 0;
	for (char[] next : toCatenate) {
	    totalSize += next.length;
	}

	char[] result = new char[totalSize];
	int writeIndex = 0;
	for (char[] next : toCatenate) {
	    if (next == null) {
		continue;
	    }
	    System.arraycopy(next, 0, result, writeIndex, next.length);
	    writeIndex += next.length;
	}
	return result;
    }

}

