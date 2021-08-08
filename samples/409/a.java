import java.lang.ref.Reference;

abstract class AbstractReferenceMap&lt;K, V&gt; extends AbstractHashedMap&lt;K, V&gt; {
    class ReferenceEntry&lt;K, V&gt; extends HashEntry&lt;K, V&gt; {
	/**
	 * Purges the specified reference
	 * @param ref  the reference to purge
	 * @return true or false
	 */
	protected boolean purge(final Reference&lt;?&gt; ref) {
	    boolean r = parent.keyType != ReferenceStrength.HARD && key == ref;
	    r = r || parent.valueType != ReferenceStrength.HARD && value == ref;
	    if (r) {
		if (parent.keyType != ReferenceStrength.HARD) {
		    ((Reference&lt;?&gt;) key).clear();
		}
		if (parent.valueType != ReferenceStrength.HARD) {
		    ((Reference&lt;?&gt;) value).clear();
		} else if (parent.purgeValues) {
		    nullValue();
		}
	    }
	    return r;
	}

	/** The parent map */
	private final AbstractReferenceMap&lt;K, V&gt; parent;

	/**
	 * This method can be overriden to provide custom logic to purge value
	 */
	protected void nullValue() {
	    value = null;
	}

    }

    /**
     * The reference type for keys.
     */
    private ReferenceStrength keyType;
    /**
     * The reference type for values.
     */
    private ReferenceStrength valueType;
    /**
     * Should the value be automatically purged when the associated key has been collected?
     */
    private boolean purgeValues;

}

