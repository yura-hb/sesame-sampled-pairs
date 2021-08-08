class Wrapper extends Enum&lt;Wrapper&gt; {
    /** Is the wrapped type a primitive other than float, double, or void? */
    public boolean isIntegral() {
	return isNumeric() && format &lt; Format.FLOAT;
    }

    private final int format;

    /** Is the wrapped type numeric (not void or object)? */
    public boolean isNumeric() {
	return (format & Format.NUM_MASK) != 0;
    }

}

