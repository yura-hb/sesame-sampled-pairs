abstract class AbstractReferenceMap&lt;K, V&gt; extends AbstractHashedMap&lt;K, V&gt; {
    class ReferenceEntry&lt;K, V&gt; extends HashEntry&lt;K, V&gt; {
	/**
	 * Gets the next entry in the bucket.
	 *
	 * @return the next entry in the bucket
	 */
	protected ReferenceEntry&lt;K, V&gt; next() {
	    return (ReferenceEntry&lt;K, V&gt;) next;
	}

    }

}

