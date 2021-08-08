class LocaleUtils {
    /**
     * Converts the given ASCII String to lower-case.
     */
    public static String toLowerString(String s) {
	int len = s.length();
	int idx = 0;
	for (; idx &lt; len; idx++) {
	    if (isUpper(s.charAt(idx))) {
		break;
	    }
	}
	if (idx == len) {
	    return s;
	}

	char[] buf = new char[len];
	for (int i = 0; i &lt; len; i++) {
	    char c = s.charAt(i);
	    buf[i] = (i &lt; idx) ? c : toLower(c);
	}
	return new String(buf);
    }

    private static boolean isUpper(char c) {
	return c &gt;= 'A' && c &lt;= 'Z';
    }

    static char toLower(char c) {
	return isUpper(c) ? (char) (c + 0x20) : c;
    }

}

