class Functions {
    /** Returns the identity function. */
    // implementation is "fully variant"; E has become a "pass-through" type
    @SuppressWarnings("unchecked")
    public static &lt;E&gt; Function&lt;E, E&gt; identity() {
	return (Function&lt;E, E&gt;) IdentityFunction.INSTANCE;
    }

}

