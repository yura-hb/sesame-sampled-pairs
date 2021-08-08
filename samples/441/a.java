class InstanceofPredicate implements Predicate&lt;Object&gt;, Serializable {
    /**
     * Factory to create the identity predicate.
     *
     * @param type  the type to check for, may not be null
     * @return the predicate
     * @throws NullPointerException if the class is null
     */
    public static Predicate&lt;Object&gt; instanceOfPredicate(final Class&lt;?&gt; type) {
	if (type == null) {
	    throw new NullPointerException("The type to check instanceof must not be null");
	}
	return new InstanceofPredicate(type);
    }

    /** The type to compare to */
    private final Class&lt;?&gt; iType;

    /**
     * Constructor that performs no validation.
     * Use &lt;code&gt;instanceOfPredicate&lt;/code&gt; if you want that.
     *
     * @param type  the type to check for
     */
    public InstanceofPredicate(final Class&lt;?&gt; type) {
	super();
	iType = type;
    }

}

