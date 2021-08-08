class Bits {
    /** Is x an element of this set?
     */
    public boolean isMember(int x) {
	Assert.check(currentState != BitsState.UNKNOWN);
	return 0 &lt;= x && x &lt; (bits.length &lt;&lt; wordshift) && (bits[x &gt;&gt;&gt; wordshift] & (1 &lt;&lt; (x & wordmask))) != 0;
    }

    protected BitsState currentState;
    public int[] bits = null;
    private final static int wordshift = 5;
    private final static int wordmask = wordlen - 1;

}

