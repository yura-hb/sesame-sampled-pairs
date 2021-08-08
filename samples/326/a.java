abstract class CharEscaper extends Escaper {
    /**
    * Returns the escaped form of a given literal string.
    *
    * @param string the literal string to be escaped
    * @return the escaped form of {@code string}
    * @throws NullPointerException if {@code string} is null
    */
    @Override
    public String escape(String string) {
	checkNotNull(string); // GWT specific check (do not optimize)
	// Inlineable fast-path loop which hands off to escapeSlow() only if needed
	int length = string.length();
	for (int index = 0; index &lt; length; index++) {
	    if (escape(string.charAt(index)) != null) {
		return escapeSlow(string, index);
	    }
	}
	return string;
    }

    /** The multiplier for padding to use when growing the escape buffer. */
    private static final int DEST_PAD_MULTIPLIER = 2;

    /**
    * Returns the escaped form of the given character, or {@code null} if this character does not
    * need to be escaped. If an empty array is returned, this effectively strips the input character
    * from the resulting text.
    *
    * &lt;p&gt;If the character does not need to be escaped, this method should return {@code null}, rather
    * than a one-character array containing the character itself. This enables the escaping algorithm
    * to perform more efficiently.
    *
    * &lt;p&gt;An escaper is expected to be able to deal with any {@code char} value, so this method should
    * not throw any exceptions.
    *
    * @param c the character to escape if necessary
    * @return the replacement characters, or {@code null} if no escaping was needed
    */
    protected abstract char[] escape(char c);

    /**
    * Returns the escaped form of a given literal string, starting at the given index. This method is
    * called by the {@link #escape(String)} method when it discovers that escaping is required. It is
    * protected to allow subclasses to override the fastpath escaping function to inline their
    * escaping test. See {@link CharEscaperBuilder} for an example usage.
    *
    * @param s the literal string to be escaped
    * @param index the index to start escaping from
    * @return the escaped form of {@code string}
    * @throws NullPointerException if {@code string} is null
    */
    protected final String escapeSlow(String s, int index) {
	int slen = s.length();

	// Get a destination buffer and setup some loop variables.
	char[] dest = Platform.charBufferFromThreadLocal();
	int destSize = dest.length;
	int destIndex = 0;
	int lastEscape = 0;

	// Loop through the rest of the string, replacing when needed into the
	// destination buffer, which gets grown as needed as well.
	for (; index &lt; slen; index++) {

	    // Get a replacement for the current character.
	    char[] r = escape(s.charAt(index));

	    // If no replacement is needed, just continue.
	    if (r == null) {
		continue;
	    }

	    int rlen = r.length;
	    int charsSkipped = index - lastEscape;

	    // This is the size needed to add the replacement, not the full size
	    // needed by the string. We only regrow when we absolutely must, and
	    // when we do grow, grow enough to avoid excessive growing. Grow.
	    int sizeNeeded = destIndex + charsSkipped + rlen;
	    if (destSize &lt; sizeNeeded) {
		destSize = sizeNeeded + DEST_PAD_MULTIPLIER * (slen - index);
		dest = growBuffer(dest, destIndex, destSize);
	    }

	    // If we have skipped any characters, we need to copy them now.
	    if (charsSkipped &gt; 0) {
		s.getChars(lastEscape, index, dest, destIndex);
		destIndex += charsSkipped;
	    }

	    // Copy the replacement string into the dest buffer as needed.
	    if (rlen &gt; 0) {
		System.arraycopy(r, 0, dest, destIndex, rlen);
		destIndex += rlen;
	    }
	    lastEscape = index + 1;
	}

	// Copy leftover characters if there are any.
	int charsLeft = slen - lastEscape;
	if (charsLeft &gt; 0) {
	    int sizeNeeded = destIndex + charsLeft;
	    if (destSize &lt; sizeNeeded) {

		// Regrow and copy, expensive! No padding as this is the final copy.
		dest = growBuffer(dest, destIndex, sizeNeeded);
	    }
	    s.getChars(lastEscape, slen, dest, destIndex);
	    destIndex = sizeNeeded;
	}
	return new String(dest, 0, destIndex);
    }

    /**
    * Helper method to grow the character buffer as needed, this only happens once in a while so it's
    * ok if it's in a method call. If the index passed in is 0 then no copying will be done.
    */
    private static char[] growBuffer(char[] dest, int index, int size) {
	if (size &lt; 0) { // overflow - should be OutOfMemoryError but GWT/j2cl don't support it
	    throw new AssertionError("Cannot increase internal buffer any further");
	}
	char[] copy = new char[size];
	if (index &gt; 0) {
	    System.arraycopy(dest, 0, copy, 0, index);
	}
	return copy;
    }

}
