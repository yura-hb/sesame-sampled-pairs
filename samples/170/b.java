class ArrayUtil {
    /** Returns the minimum value in the array */
    public static int min(int[] in) {
	int min = Integer.MAX_VALUE;
	for (int i = 0; i &lt; in.length; i++)
	    if (in[i] &lt; min)
		min = in[i];
	return min;
    }

}

