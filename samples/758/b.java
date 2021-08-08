import org.eclipse.jdt.core.compiler.CharOperation;

class Util {
    /**
     * Converts a String[] to char[][].
     */
    public static char[][] toCharArrays(String[] a) {
	int len = a.length;
	if (len == 0)
	    return CharOperation.NO_CHAR_CHAR;
	char[][] result = new char[len][];
	for (int i = 0; i &lt; len; ++i) {
	    result[i] = a[i].toCharArray();
	}
	return result;
    }

}

