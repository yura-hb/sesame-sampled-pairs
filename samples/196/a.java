class WritableComparator implements RawComparator {
    /** Parse a long from a byte array. */
    public static long readLong(byte[] bytes, int start) {
	return ((long) (readInt(bytes, start)) &lt;&lt; 32) + (readInt(bytes, start + 4) & 0xFFFFFFFFL);
    }

    /** Parse an integer from a byte array. */
    public static int readInt(byte[] bytes, int start) {
	return (((bytes[start] & 0xff) &lt;&lt; 24) + ((bytes[start + 1] & 0xff) &lt;&lt; 16) + ((bytes[start + 2] & 0xff) &lt;&lt; 8)
		+ ((bytes[start + 3] & 0xff)));

    }

}

