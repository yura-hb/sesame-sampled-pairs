class Parser implements DTDConstants {
    /**
     * Add a char to the string buffer.
     */
    void addString(int c) {
	if (strpos == str.length) {
	    char newstr[] = new char[str.length + 128];
	    System.arraycopy(str, 0, newstr, 0, str.length);
	    str = newstr;
	}
	str[strpos++] = (char) c;
    }

    private int strpos = 0;
    private char str[] = new char[128];

}

