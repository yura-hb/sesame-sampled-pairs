class StringValueTransformer&lt;T&gt; implements Transformer&lt;T, String&gt;, Serializable {
    /**
     * Factory returning the singleton instance.
     *
     * @param &lt;T&gt;  the input type
     * @return the singleton instance
     * @since 3.1
     */
    @SuppressWarnings("unchecked")
    public static &lt;T&gt; Transformer&lt;T, String&gt; stringValueTransformer() {
	return (Transformer&lt;T, String&gt;) INSTANCE;
    }

    /** Singleton predicate instance */
    private static final Transformer&lt;Object, String&gt; INSTANCE = new StringValueTransformer&lt;&gt;();

}

