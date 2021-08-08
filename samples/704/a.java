abstract class ImmutableMultimap&lt;K, V&gt; extends BaseImmutableMultimap&lt;K, V&gt; implements Serializable {
    class Builder&lt;K, V&gt; {
	/**
	* Specifies the ordering of the generated multimap's keys.
	*
	* @since 8.0
	*/
	@CanIgnoreReturnValue
	public Builder&lt;K, V&gt; orderKeysBy(Comparator&lt;? super K&gt; keyComparator) {
	    this.keyComparator = checkNotNull(keyComparator);
	    return this;
	}

	@MonotonicNonNull
	Comparator&lt;? super K&gt; keyComparator;

    }

}

