import static com.google.common.collect.CollectPreconditions.checkEntryNotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

abstract class ImmutableMultimap&lt;K, V&gt; extends BaseImmutableMultimap&lt;K, V&gt; implements Serializable {
    class Builder&lt;K, V&gt; {
	/**
	* Adds an entry to the built multimap.
	*
	* @since 11.0
	*/
	@CanIgnoreReturnValue
	public Builder&lt;K, V&gt; put(Entry&lt;? extends K, ? extends V&gt; entry) {
	    return put(entry.getKey(), entry.getValue());
	}

	Map&lt;K, Collection&lt;V&gt;&gt; builderMap;

	/** Adds a key-value mapping to the built multimap. */
	@CanIgnoreReturnValue
	public Builder&lt;K, V&gt; put(K key, V value) {
	    checkEntryNotNull(key, value);
	    Collection&lt;V&gt; valueCollection = builderMap.get(key);
	    if (valueCollection == null) {
		builderMap.put(key, valueCollection = newMutableValueCollection());
	    }
	    valueCollection.add(value);
	    return this;
	}

	Collection&lt;V&gt; newMutableValueCollection() {
	    return new ArrayList&lt;&gt;();
	}

    }

}

