class NumberUtils {
    /**
     * &lt;p&gt;Compares two {@code int} values numerically. This is the same functionality as provided in Java 7.&lt;/p&gt;
     *
     * @param x the first {@code int} to compare
     * @param y the second {@code int} to compare
     * @return the value {@code 0} if {@code x == y};
     *         a value less than {@code 0} if {@code x &lt; y}; and
     *         a value greater than {@code 0} if {@code x &gt; y}
     * @since 3.4
     */
    public static int compare(final int x, final int y) {
	if (x == y) {
	    return 0;
	}
	return x &lt; y ? -1 : 1;
    }

}

