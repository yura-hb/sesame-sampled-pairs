class Convert {
    /** Convert string to integer.
     */
    public static int string2int(String s, int radix) throws NumberFormatException {
	if (radix == 10) {
	    return Integer.parseInt(s, radix);
	} else {
	    char[] cs = s.toCharArray();
	    int limit = Integer.MAX_VALUE / (radix / 2);
	    int n = 0;
	    for (char c : cs) {
		int d = Character.digit(c, radix);
		if (n &lt; 0 || n &gt; limit || n * radix &gt; Integer.MAX_VALUE - d)
		    throw new NumberFormatException();
		n = n * radix + d;
	    }
	    return n;
	}
    }

}

