class Convert {
    /** Escape all unicode characters in string.
     */
    public static String escapeUnicode(String s) {
	int len = s.length();
	int i = 0;
	while (i &lt; len) {
	    char ch = s.charAt(i);
	    if (ch &gt; 255) {
		StringBuilder buf = new StringBuilder();
		buf.append(s.substring(0, i));
		while (i &lt; len) {
		    ch = s.charAt(i);
		    if (ch &gt; 255) {
			buf.append("\\u");
			buf.append(Character.forDigit((ch &gt;&gt; 12) % 16, 16));
			buf.append(Character.forDigit((ch &gt;&gt; 8) % 16, 16));
			buf.append(Character.forDigit((ch &gt;&gt; 4) % 16, 16));
			buf.append(Character.forDigit((ch) % 16, 16));
		    } else {
			buf.append(ch);
		    }
		    i++;
		}
		s = buf.toString();
	    } else {
		i++;
	    }
	}
	return s;
    }

}

