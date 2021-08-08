import java.lang.reflect.Array;

class ArrayUtils {
    /**
     * &lt;p&gt;Inserts the specified element at the specified position in the array.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     *
     * &lt;p&gt;This method returns a new array with the same elements of the input
     * array plus the given element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * &lt;p&gt;If the input array is {@code null}, a new one element array is returned
     *  whose component type is the same as the element.
     *
     * &lt;pre&gt;
     * ArrayUtils.add([1], 0, 2)         = [2, 1]
     * ArrayUtils.add([2, 6], 2, 3)      = [2, 6, 3]
     * ArrayUtils.add([2, 6], 0, 1)      = [1, 2, 6]
     * ArrayUtils.add([2, 6, 3], 2, 1)   = [2, 6, 1, 3]
     * &lt;/pre&gt;
     *
     * @param array  the array to add the element to, may be {@code null}
     * @param index  the position of the new object
     * @param element  the object to add
     * @return A new array containing the existing elements and the new element
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index &lt; 0 || index &gt; array.length).
     * @deprecated this method has been superseded by {@link #insert(int, byte[], byte...)} and
     * may be removed in a future release. Please note the handling of {@code null} input arrays differs
     * in the new method: inserting {@code X} into a {@code null} array results in {@code null} not {@code X}.
     */
    @Deprecated
    public static byte[] add(final byte[] array, final int index, final byte element) {
	return (byte[]) add(array, index, Byte.valueOf(element), Byte.TYPE);
    }

    /**
     * Underlying implementation of add(array, index, element) methods.
     * The last parameter is the class, which may not equal element.getClass
     * for primitives.
     *
     * @param array  the array to add the element to, may be {@code null}
     * @param index  the position of the new object
     * @param element  the object to add
     * @param clss the type of the element being added
     * @return A new array containing the existing elements and the new element
     */
    private static Object add(final Object array, final int index, final Object element, final Class&lt;?&gt; clss) {
	if (array == null) {
	    if (index != 0) {
		throw new IndexOutOfBoundsException("Index: " + index + ", Length: 0");
	    }
	    final Object joinedArray = Array.newInstance(clss, 1);
	    Array.set(joinedArray, 0, element);
	    return joinedArray;
	}
	final int length = Array.getLength(array);
	if (index &gt; length || index &lt; 0) {
	    throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
	}
	final Object result = Array.newInstance(clss, length + 1);
	System.arraycopy(array, 0, result, 0, index);
	Array.set(result, index, element);
	if (index &lt; length) {
	    System.arraycopy(array, index, result, index + 1, length - index);
	}
	return result;
    }

}

