import java.util.concurrent.atomic.AtomicReference;
import jdk.internal.vm.compiler.collections.EconomicMap;
import jdk.internal.vm.compiler.collections.Equivalence;
import jdk.internal.vm.compiler.collections.UnmodifiableEconomicMap;
import jdk.internal.vm.compiler.collections.UnmodifiableMapCursor;

class ModifiableOptionValues extends OptionValues {
    /**
     * Updates this object with the key/value pairs in {@code values}.
     *
     * @see #UNSET_KEY
     */
    public void update(UnmodifiableEconomicMap&lt;OptionKey&lt;?&gt;, Object&gt; values) {
	if (values.isEmpty()) {
	    return;
	}
	UnmodifiableEconomicMap&lt;OptionKey&lt;?&gt;, Object&gt; expect;
	EconomicMap&lt;OptionKey&lt;?&gt;, Object&gt; newMap;
	do {
	    expect = v.get();
	    newMap = EconomicMap.create(Equivalence.IDENTITY, expect);
	    UnmodifiableMapCursor&lt;OptionKey&lt;?&gt;, Object&gt; cursor = values.getEntries();
	    while (cursor.advance()) {
		OptionKey&lt;?&gt; key = cursor.getKey();
		Object value = cursor.getValue();
		if (value == UNSET_KEY) {
		    newMap.removeKey(key);
		} else {
		    key.update(newMap, value);
		    // Need to do the null encoding here as `key.update()` doesn't do it
		    newMap.put(key, encodeNull(value));
		}
	    }
	} while (!v.compareAndSet(expect, newMap));
    }

    private final AtomicReference&lt;UnmodifiableEconomicMap&lt;OptionKey&lt;?&gt;, Object&gt;&gt; v = new AtomicReference&lt;&gt;();
    /**
     * Value that can be used in {@link #update(UnmodifiableEconomicMap)} and
     * {@link #update(OptionKey, Object)} to remove an explicitly set value for a key such that
     * {@link OptionKey#hasBeenSet(OptionValues)} will return {@code false} for the key.
     */
    public static final Object UNSET_KEY = new Object();

}

