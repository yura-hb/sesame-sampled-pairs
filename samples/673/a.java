class CharArrayCache {
    /**
    * Returns the key corresponding to the value. Returns null if the
    * receiver doesn't contain the value.
    * @param value int the value that we are looking for
    * @return Object
    */
    public char[] returnKeyFor(int value) {
	for (int i = this.keyTable.length; i-- &gt; 0;) {
	    if (this.valueTable[i] == value) {
		return this.keyTable[i];
	    }
	}
	return null;
    }

    public char[] keyTable[];
    public int valueTable[];

}

