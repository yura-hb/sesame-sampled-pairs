abstract class Invokable&lt;T, R&gt; extends Element implements GenericDeclaration {
    /** Returns the type of {@code T}. */
    // Overridden in TypeToken#method() and TypeToken#constructor()
    @SuppressWarnings("unchecked") // The declaring class is T.
    @Override
    public TypeToken&lt;T&gt; getOwnerType() {
	return (TypeToken&lt;T&gt;) TypeToken.of(getDeclaringClass());
    }

    @SuppressWarnings("unchecked") // The declaring class is T's raw class, or one of its supertypes.
    @Override
    public final Class&lt;? super T&gt; getDeclaringClass() {
	return (Class&lt;? super T&gt;) super.getDeclaringClass();
    }

}

