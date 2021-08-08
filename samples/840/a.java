class WritableUtils {
    /**
     * Get the encoded length if an integer is stored in a variable-length format
     * @return the encoded length
     */
    public static int getVIntSize(long i) {
	if (i &gt;= -112 && i &lt;= 127) {
	    return 1;
	}

	if (i &lt; 0) {
	    i ^= -1L; // take one's complement'
	}
	// find the number of bytes with non-leading zeros
	int dataBits = Long.SIZE - Long.numberOfLeadingZeros(i);
	// find the number of data bytes + length byte
	return (dataBits + 7) / 8 + 1;
    }

}

