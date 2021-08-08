import com.google.common.collect.ImmutableMap;

class ImmutableTypeToInstanceMap&lt;B&gt; extends ForwardingMap&lt;TypeToken&lt;? extends B&gt;, B&gt; implements TypeToInstanceMap&lt;B&gt; {
    /** Returns an empty type to instance map. */
    public static &lt;B&gt; ImmutableTypeToInstanceMap&lt;B&gt; of() {
	return new ImmutableTypeToInstanceMap&lt;B&gt;(ImmutableMap.&lt;TypeToken&lt;? extends B&gt;, B&gt;of());
    }

    private final ImmutableMap&lt;TypeToken&lt;? extends B&gt;, B&gt; delegate;

    private ImmutableTypeToInstanceMap(ImmutableMap&lt;TypeToken&lt;? extends B&gt;, B&gt; delegate) {
	this.delegate = delegate;
    }

}

