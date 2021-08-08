import com.google.common.primitives.Ints;

class Maps {
    /**
    * Returns a capacity that is sufficient to keep the map from being resized as long as it grows no
    * larger than expectedSize and the load factor is ≥ its default (0.75).
    */
    static int capacity(int expectedSize) {
	if (expectedSize &lt; 3) {
	    checkNonnegative(expectedSize, "expectedSize");
	    return expectedSize + 1;
	}
	if (expectedSize &lt; Ints.MAX_POWER_OF_TWO) {
	    // This is the calculation used in JDK8 to resize when a putAll
	    // happens; it seems to be the most conservative calculation we
	    // can make.  0.75 is the default load factor.
	    return (int) ((float) expectedSize / 0.75F + 1.0F);
	}
	return Integer.MAX_VALUE; // any large value
    }

}

