import java.util.Vector;

class DefaultListModel&lt;E&gt; extends AbstractListModel&lt;E&gt; {
    /**
     * Deletes the components at the specified range of indexes.
     * The removal is inclusive, so specifying a range of (1,5)
     * removes the component at index 1 and the component at index 5,
     * as well as all components in between.
     *
     * @param      fromIndex the index of the lower end of the range
     * @param      toIndex   the index of the upper end of the range
     * @throws ArrayIndexOutOfBoundsException if the index was invalid
     * @throws IllegalArgumentException if {@code fromIndex &gt; toIndex}
     * @see        #remove(int)
     */
    public void removeRange(int fromIndex, int toIndex) {
	if (fromIndex &gt; toIndex) {
	    throw new IllegalArgumentException("fromIndex must be &lt;= toIndex");
	}
	for (int i = toIndex; i &gt;= fromIndex; i--) {
	    delegate.removeElementAt(i);
	}
	fireIntervalRemoved(this, fromIndex, toIndex);
    }

    private Vector&lt;E&gt; delegate = new Vector&lt;E&gt;();

}

