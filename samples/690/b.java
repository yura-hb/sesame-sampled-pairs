import java.util.AbstractList;

class IdentityArrayList&lt;E&gt; extends AbstractList&lt;E&gt; implements List&lt;E&gt;, RandomAccess {
    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    public void clear() {
	modCount++;

	// Let gc do its work
	for (int i = 0; i &lt; size; i++)
	    elementData[i] = null;

	size = 0;
    }

    /**
     * The size of the IdentityArrayList (the number of elements it contains).
     *
     * @serial
     */
    private int size;
    /**
     * The array buffer into which the elements of the IdentityArrayList are stored.
     * The capacity of the IdentityArrayList is the length of this array buffer.
     */
    private transient Object[] elementData;

}

