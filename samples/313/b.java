import java.util.ArrayList;
import java.util.Map;

class ArrayListMultimap&lt;K, V&gt; extends ArrayListMultimapGwtSerializationDependencies&lt;K, V&gt; {
    /**
    * Reduces the memory used by this {@code ArrayListMultimap}, if feasible.
    *
    * @deprecated For a {@link ListMultimap} that automatically trims to size, use {@link
    *     ImmutableListMultimap}. If you need a mutable collection, remove the {@code trimToSize}
    *     call, or switch to a {@code HashMap&lt;K, ArrayList&lt;V&gt;&gt;}.
    */
    @Deprecated
    public void trimToSize() {
	for (Collection&lt;V&gt; collection : backingMap().values()) {
	    ArrayList&lt;V&gt; arrayList = (ArrayList&lt;V&gt;) collection;
	    arrayList.trimToSize();
	}
    }

}

