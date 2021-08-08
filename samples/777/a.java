import java.util.BitSet;

abstract class CharMatcher implements Predicate&lt;Character&gt; {
    /** Sets bits in {@code table} matched by this matcher. */
    @GwtIncompatible // used only from other GwtIncompatible code
    void setBits(BitSet table) {
	for (int c = Character.MAX_VALUE; c &gt;= Character.MIN_VALUE; c--) {
	    if (matches((char) c)) {
		table.set(c);
	    }
	}
    }

    /** Determines a true or false value for the given character. */
    public abstract boolean matches(char c);

}

