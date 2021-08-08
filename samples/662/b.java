class SplittableRandom {
    /**
     * Fills a user-supplied byte array with generated pseudorandom bytes.
     *
     * @param  bytes the byte array to fill with pseudorandom bytes
     * @throws NullPointerException if bytes is null
     * @since  10
     */
    public void nextBytes(byte[] bytes) {
	int i = 0;
	int len = bytes.length;
	for (int words = len &gt;&gt; 3; words-- &gt; 0;) {
	    long rnd = nextLong();
	    for (int n = 8; n-- &gt; 0; rnd &gt;&gt;&gt;= Byte.SIZE)
		bytes[i++] = (byte) rnd;
	}
	if (i &lt; len)
	    for (long rnd = nextLong(); i &lt; len; rnd &gt;&gt;&gt;= Byte.SIZE)
		bytes[i++] = (byte) rnd;
    }

    /**
     * The seed. Updated only via method nextSeed.
     */
    private long seed;
    /**
     * The step value.
     */
    private final long gamma;

    /**
     * Returns a pseudorandom {@code long} value.
     *
     * @return a pseudorandom {@code long} value
     */
    public long nextLong() {
	return mix64(nextSeed());
    }

    /**
     * Adds gamma to seed.
     */
    private long nextSeed() {
	return seed += gamma;
    }

    /**
     * Computes Stafford variant 13 of 64bit mix function.
     */
    private static long mix64(long z) {
	z = (z ^ (z &gt;&gt;&gt; 30)) * 0xbf58476d1ce4e5b9L;
	z = (z ^ (z &gt;&gt;&gt; 27)) * 0x94d049bb133111ebL;
	return z ^ (z &gt;&gt;&gt; 31);
    }

}

