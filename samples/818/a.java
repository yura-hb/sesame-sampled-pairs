class ObjectArrayListIterator&lt;E&gt; extends ObjectArrayIterator&lt;E&gt; implements ResettableListIterator&lt;E&gt; {
    /**
     * Resets the iterator back to the start index.
     */
    @Override
    public void reset() {
	super.reset();
	this.lastItemIndex = -1;
    }

    /**
     * Holds the index of the last item returned by a call to &lt;code&gt;next()&lt;/code&gt;
     * or &lt;code&gt;previous()&lt;/code&gt;. This is set to &lt;code&gt;-1&lt;/code&gt; if neither method
     * has yet been invoked. &lt;code&gt;lastItemIndex&lt;/code&gt; is used to to implement the
     * {@link #set} method.
     */
    private int lastItemIndex = -1;

}

