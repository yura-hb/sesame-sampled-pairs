class CharSet {
    /**
     * Returns the intersection of two CharSets.
     */
    public CharSet intersection(CharSet that) {
	return new CharSet(doIntersection(that.chars));
    }

    /**
     * The structure containing the set information.  The characters
     * in this array are organized into pairs, each pair representing
     * a range of characters contained in the set
     */
    private int[] chars;

    /**
     * The internal implementation of the two intersection functions
     */
    private int[] doIntersection(int[] c2) {
	int[] result = new int[chars.length + c2.length];

	int i = 0;
	int j = 0;
	int oldI;
	int oldJ;
	int index = 0;

	// iterate until we've exhausted one of the operands
	while (i &lt; chars.length && j &lt; c2.length) {

	    // advance j until it points to a character that is larger than
	    // the one i points to.  If this is the beginning of a one-
	    // character range, advance j to point to the end
	    if (i &lt; chars.length && i % 2 == 0) {
		while (j &lt; c2.length && c2[j] &lt; chars[i]) {
		    ++j;
		}
		if (j &lt; c2.length && j % 2 == 0 && c2[j] == chars[i]) {
		    ++j;
		}
	    }

	    // if j points to the endpoint of a range, save the current
	    // value of i, then advance i until it reaches a character
	    // which is larger than the character pointed at
	    // by j.  All of the characters we've advanced over (except
	    // the one currently pointed to by i) are added to the result
	    oldI = i;
	    while (j % 2 == 1 && i &lt; chars.length && chars[i] &lt;= c2[j]) {
		++i;
	    }
	    for (int k = oldI; k &lt; i; k++) {
		result[index++] = chars[k];
	    }

	    // if i points to the endpoint of a range, save the current
	    // value of j, then advance j until it reaches a character
	    // which is larger than the character pointed at
	    // by i.  All of the characters we've advanced over (except
	    // the one currently pointed to by i) are added to the result
	    oldJ = j;
	    while (i % 2 == 1 && j &lt; c2.length && c2[j] &lt;= chars[i]) {
		++j;
	    }
	    for (int k = oldJ; k &lt; j; k++) {
		result[index++] = c2[k];
	    }

	    // advance i until it points to a character larger than j
	    // If it points at the beginning of a one-character range,
	    // advance it to the end of that range
	    if (j &lt; c2.length && j % 2 == 0) {
		while (i &lt; chars.length && chars[i] &lt; c2[j]) {
		    ++i;
		}
		if (i &lt; chars.length && i % 2 == 0 && c2[j] == chars[i]) {
		    ++i;
		}
	    }
	}

	if (result.length &gt; index) {
	    int[] tmpbuf = new int[index];
	    System.arraycopy(result, 0, tmpbuf, 0, index);
	    return tmpbuf;
	}

	return result;
    }

    /**
     * Creates a CharSet, initializing it from the internal storage
     * of another CharSet (this function performs no error checking
     * on "chars", so if it's malformed, undefined behavior will result)
     */
    private CharSet(int[] chars) {
	this.chars = chars;
    }

}

