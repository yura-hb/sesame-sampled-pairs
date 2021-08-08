class ArrayUtil {
    /** Returns the maximum value in the array */
    public static int max(int[] in) {
	int max = Integer.MIN_VALUE;
	for (int i = 0; i &lt; in.length; i++)
	    if (in[i] &gt; max)
		max = in[i];
	return max;
    }

}

