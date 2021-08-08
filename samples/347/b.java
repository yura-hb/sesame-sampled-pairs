import java.util.Arrays;

class Bits {
    /** Include x in this set.
     */
    public void incl(int x) {
	Assert.check(currentState != BitsState.UNKNOWN);
	Assert.check(x &gt;= 0);
	sizeTo((x &gt;&gt;&gt; wordshift) + 1);
	bits[x &gt;&gt;&gt; wordshift] = bits[x &gt;&gt;&gt; wordshift] | (1 &lt;&lt; (x & wordmask));
	currentState = BitsState.NORMAL;
    }

    protected BitsState currentState;
    private final static int wordshift = 5;
    public int[] bits = null;
    private final static int wordmask = wordlen - 1;

    protected void sizeTo(int len) {
	if (bits.length &lt; len) {
	    bits = Arrays.copyOf(bits, len);
	}
    }

}

