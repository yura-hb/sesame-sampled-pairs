import java.util.Iterator;

class EntrySetMapIterator&lt;K, V&gt; implements MapIterator&lt;K, V&gt;, ResettableIterator&lt;K&gt; {
    /**
     * Removes the last returned key from the underlying &lt;code&gt;Map&lt;/code&gt;.
     * &lt;p&gt;
     * This method can be called once per call to &lt;code&gt;next()&lt;/code&gt;.
     *
     * @throws UnsupportedOperationException if remove is not supported by the map
     * @throws IllegalStateException if &lt;code&gt;next()&lt;/code&gt; has not yet been called
     * @throws IllegalStateException if &lt;code&gt;remove()&lt;/code&gt; has already been called
     *  since the last call to &lt;code&gt;next()&lt;/code&gt;
     */
    @Override
    public void remove() {
	if (canRemove == false) {
	    throw new IllegalStateException("Iterator remove() can only be called once after next()");
	}
	iterator.remove();
	last = null;
	canRemove = false;
    }

    private boolean canRemove = false;
    private Iterator&lt;Map.Entry&lt;K, V&gt;&gt; iterator;
    private Map.Entry&lt;K, V&gt; last;

}

