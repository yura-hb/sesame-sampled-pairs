import static com.google.common.primitives.UnsignedInts.compare;

class UnsignedInteger extends Number implements Comparable&lt;UnsignedInteger&gt; {
    /**
    * Compares this unsigned integer to another unsigned integer. Returns {@code 0} if they are
    * equal, a negative number if {@code this &lt; other}, and a positive number if {@code this &gt;
    * other}.
    */
    @Override
    public int compareTo(UnsignedInteger other) {
	checkNotNull(other);
	return compare(value, other.value);
    }

    private final int value;

}

