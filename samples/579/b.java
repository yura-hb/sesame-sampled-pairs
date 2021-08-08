class Parser implements DTDConstants {
    /**
     * Get the string that's been accumulated.
     */
    String getString(int pos) {
	char newStr[] = new char[strpos - pos];
	System.arraycopy(str, pos, newStr, 0, strpos - pos);
	strpos = pos;
	return new String(newStr);
    }

    private int strpos = 0;
    private char str[] = new char[128];

}

