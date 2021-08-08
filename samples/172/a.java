class CollectionUtils {
    /**
     * Reverses the order of the given array.
     *
     * @param array  the array to reverse
     */
    public static void reverseArray(final Object[] array) {
	int i = 0;
	int j = array.length - 1;
	Object tmp;

	while (j &gt; i) {
	    tmp = array[j];
	    array[j] = array[i];
	    array[i] = tmp;
	    j--;
	    i++;
	}
    }

}

