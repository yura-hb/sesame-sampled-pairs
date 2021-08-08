class IntMath {
    /**
    * Returns the arithmetic mean of {@code x} and {@code y}, rounded towards negative infinity. This
    * method is overflow resilient.
    *
    * @since 14.0
    */
    public static int mean(int x, int y) {
	// Efficient method for computing the arithmetic mean.
	// The alternative (x + y) / 2 fails for large values.
	// The alternative (x + y) &gt;&gt;&gt; 1 fails for negative values.
	return (x & y) + ((x ^ y) &gt;&gt; 1);
    }

}

