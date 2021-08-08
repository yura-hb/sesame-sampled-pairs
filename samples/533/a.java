class TransformedList&lt;E&gt; extends TransformedCollection&lt;E&gt; implements List&lt;E&gt; {
    /**
     * Gets the decorated list.
     *
     * @return the decorated list
     */
    protected List&lt;E&gt; getList() {
	return (List&lt;E&gt;) decorated();
    }

}

