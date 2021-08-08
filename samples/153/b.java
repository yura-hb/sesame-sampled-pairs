class IdentityArrayList&lt;E&gt; extends AbstractList&lt;E&gt; implements List&lt;E&gt;, RandomAccess {
    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E set(int index, E element) {
	rangeCheck(index);

	@SuppressWarnings("unchecked")
	E oldValue = (E) elementData[index];
	elementData[index] = element;
	return oldValue;
    }

    /**
     * The array buffer into which the elements of the IdentityArrayList are stored.
     * The capacity of the IdentityArrayList is the length of this array buffer.
     */
    private transient Object[] elementData;
    /**
     * The size of the IdentityArrayList (the number of elements it contains).
     *
     * @serial
     */
    private int size;

    /**
     * Checks if the given index is in range.  If not, throws an appropriate
     * runtime exception.  This method does *not* check if the index is
     * negative: It is always used immediately prior to an array access,
     * which throws an ArrayIndexOutOfBoundsException if index is negative.
     */
    private void rangeCheck(int index) {
	if (index &gt;= size)
	    throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message.
     * Of the many possible refactorings of the error handling code,
     * this "outlining" performs best with both server and client VMs.
     */
    private String outOfBoundsMsg(int index) {
	return "Index: " + index + ", Size: " + size;
    }

}

