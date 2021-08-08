class MapMakerInternalMap&lt;K, V, E, S&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    abstract class Segment&lt;K, V, E, S&gt; extends ReentrantLock {
	/** Returns a copy of the given {@code entry}. */
	E copyEntry(E original, E newNext) {
	    return this.map.entryHelper.copy(self(), original, newNext);
	}

	@Weak
	final MapMakerInternalMap&lt;K, V, E, S&gt; map;

	/**
	 * Returns {@code this} up-casted to the specific {@link Segment} implementation type {@code S}.
	 *
	 * &lt;p&gt;This method exists so that the {@link Segment} code can be generic in terms of {@code S},
	 * the type of the concrete implementation.
	 */
	abstract S self();

    }

    /** Strategy for handling entries and segments in a type-safe and efficient manner. */
    final transient InternalEntryHelper&lt;K, V, E, S&gt; entryHelper;

    interface InternalEntryHelper&lt;K, V, E, S&gt; {
	/** Strategy for handling entries and segments in a type-safe and efficient manner. */
	final transient InternalEntryHelper&lt;K, V, E, S&gt; entryHelper;

	/**
	* Returns a freshly created entry, typed at the {@code E} type, for the given {@code segment},
	* that is a copy of the given {@code entry}.
	*/
	E copy(S segment, E entry, @Nullable E newNext);

    }

}

