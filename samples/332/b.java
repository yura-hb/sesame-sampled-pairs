import java.util.Collection;

class Maps {
    /** Returns a map from the ith element of list to i. */
    static &lt;E&gt; ImmutableMap&lt;E, Integer&gt; indexMap(Collection&lt;E&gt; list) {
	ImmutableMap.Builder&lt;E, Integer&gt; builder = new ImmutableMap.Builder&lt;&gt;(list.size());
	int i = 0;
	for (E e : list) {
	    builder.put(e, i++);
	}
	return builder.build();
    }

}

