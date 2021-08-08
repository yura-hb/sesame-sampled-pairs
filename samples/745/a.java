class CharArrayBuffer {
    /**
    * Returns the entire contents of the buffer as one
    * char[] or null if nothing has been put in the buffer.
    */
    public char[] getContents() {
	if (this.end == 0)
	    return null;

	// determine the length of the array
	int length = 0;
	for (int i = 0; i &lt; this.end; i++)
	    length += this.ranges[i][1];

	if (length &gt; 0) {
	    char[] result = new char[length];
	    int current = 0;
	    // copy the results
	    for (int i = 0; i &lt; this.end; i++) {
		int[] range = this.ranges[i];
		int length2 = range[1];
		System.arraycopy(this.buffer[i], range[0], result, current, length2);
		current += length2;
	    }
	    return result;
	}
	return null;
    }

    /**
     * The end of the buffer
     */
    protected int end;
    /**
     * A buffer of ranges which is maintained along with
     * the buffer.  Ranges are of the form {start, length}.
     * Enables append(char[] array, int start, int end).
     */
    protected int[][] ranges;
    /**
     * This is the buffer of char arrays which must be appended together
     * during the getContents method.
     */
    protected char[][] buffer;

}

