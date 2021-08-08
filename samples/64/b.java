class Integer extends Number implements Comparable&lt;Integer&gt; {
    /**
     * Compares two {@code int} values numerically treating the values
     * as unsigned.
     *
     * @param  x the first {@code int} to compare
     * @param  y the second {@code int} to compare
     * @return the value {@code 0} if {@code x == y}; a value less
     *         than {@code 0} if {@code x &lt; y} as unsigned values; and
     *         a value greater than {@code 0} if {@code x &gt; y} as
     *         unsigned values
     * @since 1.8
     */
    public static int compareUnsigned(int x, int y) {
	return compare(x + MIN_VALUE, y + MIN_VALUE);
    }

    /**
     * A constant holding the minimum value an {@code int} can
     * have, -2&lt;sup&gt;31&lt;/sup&gt;.
     */
    @Native
    public static final int MIN_VALUE = 0x80000000;

    /**
     * Compares two {@code int} values numerically.
     * The value returned is identical to what would be returned by:
     * &lt;pre&gt;
     *    Integer.valueOf(x).compareTo(Integer.valueOf(y))
     * &lt;/pre&gt;
     *
     * @param  x the first {@code int} to compare
     * @param  y the second {@code int} to compare
     * @return the value {@code 0} if {@code x == y};
     *         a value less than {@code 0} if {@code x &lt; y}; and
     *         a value greater than {@code 0} if {@code x &gt; y}
     * @since 1.7
     */
    public static int compare(int x, int y) {
	return (x &lt; y) ? -1 : ((x == y) ? 0 : 1);
    }

}

