import static com.google.common.base.Preconditions.checkPositionIndexes;

class Bytes {
    /**
    * Reverses the elements of {@code array}. This is equivalent to {@code
    * Collections.reverse(Bytes.asList(array))}, but is likely to be more efficient.
    *
    * @since 23.1
    */
    public static void reverse(byte[] array) {
	checkNotNull(array);
	reverse(array, 0, array.length);
    }

    /**
    * Reverses the elements of {@code array} between {@code fromIndex} inclusive and {@code toIndex}
    * exclusive. This is equivalent to {@code
    * Collections.reverse(Bytes.asList(array).subList(fromIndex, toIndex))}, but is likely to be more
    * efficient.
    *
    * @throws IndexOutOfBoundsException if {@code fromIndex &lt; 0}, {@code toIndex &gt; array.length}, or
    *     {@code toIndex &gt; fromIndex}
    * @since 23.1
    */
    public static void reverse(byte[] array, int fromIndex, int toIndex) {
	checkNotNull(array);
	checkPositionIndexes(fromIndex, toIndex, array.length);
	for (int i = fromIndex, j = toIndex - 1; i &lt; j; i++, j--) {
	    byte tmp = array[i];
	    array[i] = array[j];
	    array[j] = tmp;
	}
    }

}

