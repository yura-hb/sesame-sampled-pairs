import java.util.Locale;

class StringUtils {
    /**
     * Convert a byte to a hex string.
     * @see #byteToHexString(byte[])
     * @see #byteToHexString(byte[], int, int)
     * @param b byte
     * @return byte's hex value as a String
     */
    public static String byteToHexString(byte b) {
	return byteToHexString(new byte[] { b });
    }

    /** Same as byteToHexString(bytes, 0, bytes.length). */
    public static String byteToHexString(byte bytes[]) {
	return byteToHexString(bytes, 0, bytes.length);
    }

    /**
     * Given an array of bytes it will convert the bytes to a hex string
     * representation of the bytes
     * @param bytes
     * @param start start index, inclusively
     * @param end end index, exclusively
     * @return hex string representation of the byte array
     */
    public static String byteToHexString(byte[] bytes, int start, int end) {
	if (bytes == null) {
	    throw new IllegalArgumentException("bytes == null");
	}
	StringBuilder s = new StringBuilder();
	for (int i = start; i &lt; end; i++) {
	    s.append(format("%02x", bytes[i]));
	}
	return s.toString();
    }

    /** The same as String.format(Locale.ENGLISH, format, objects). */
    public static String format(final String format, final Object... objects) {
	return String.format(Locale.ENGLISH, format, objects);
    }

}

