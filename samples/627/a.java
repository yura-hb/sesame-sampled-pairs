abstract class StripedBuffer&lt;E&gt; implements Buffer&lt;E&gt; {
    /** Returns the closest power-of-two at or higher than the given value. */
    static int ceilingNextPowerOfTwo(int x) {
	// From Hacker's Delight, Chapter 3, Harry S. Warren Jr.
	return 1 &lt;&lt; (Integer.SIZE - Integer.numberOfLeadingZeros(x - 1));
    }

}

