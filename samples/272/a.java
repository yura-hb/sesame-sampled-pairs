class Caffeine&lt;K, V&gt; {
    /**
    * Sets the minimum total size for the internal data structures. Providing a large enough estimate
    * at construction time avoids the need for expensive resizing operations later, but setting this
    * value unnecessarily high wastes memory.
    *
    * @param initialCapacity minimum total size for the internal data structures
    * @return this {@code Caffeine} instance (for chaining)
    * @throws IllegalArgumentException if {@code initialCapacity} is negative
    * @throws IllegalStateException if an initial capacity was already set
    */
    @NonNull
    public Caffeine&lt;K, V&gt; initialCapacity(@NonNegative int initialCapacity) {
	requireState(this.initialCapacity == UNSET_INT, "initial capacity was already set to %s", this.initialCapacity);
	requireArgument(initialCapacity &gt;= 0);
	this.initialCapacity = initialCapacity;
	return this;
    }

    int initialCapacity = UNSET_INT;
    static final int UNSET_INT = -1;

    /** Ensures that the state expression is true. */
    static void requireState(boolean expression, String template, Object... args) {
	if (!expression) {
	    throw new IllegalStateException(String.format(template, args));
	}
    }

    /** Ensures that the argument expression is true. */
    static void requireArgument(boolean expression) {
	if (!expression) {
	    throw new IllegalArgumentException();
	}
    }

}

