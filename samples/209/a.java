abstract class Invokable&lt;T, R&gt; extends Element implements GenericDeclaration {
    /** Explicitly specifies the return type of this {@code Invokable}. */
    public final &lt;R1 extends R&gt; Invokable&lt;T, R1&gt; returning(TypeToken&lt;R1&gt; returnType) {
	if (!returnType.isSupertypeOf(getReturnType())) {
	    throw new IllegalArgumentException(
		    "Invokable is known to return " + getReturnType() + ", not " + returnType);
	}
	@SuppressWarnings("unchecked") // guarded by previous check
	Invokable&lt;T, R1&gt; specialized = (Invokable&lt;T, R1&gt;) this;
	return specialized;
    }

    /** Returns the return type of this {@code Invokable}. */
    // All subclasses are owned by us and we'll make sure to get the R type right.
    @SuppressWarnings("unchecked")
    public final TypeToken&lt;? extends R&gt; getReturnType() {
	return (TypeToken&lt;? extends R&gt;) TypeToken.of(getGenericReturnType());
    }

    abstract Type getGenericReturnType();

}

