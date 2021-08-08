import java.util.Map;

class AppContext {
    /**
     * Maps the specified {@code key} to the specified
     * {@code value} in this AppContext.  Neither the key nor the
     * value can be {@code null}.
     * &lt;p&gt;
     * The value can be retrieved by calling the {@code get} method
     * with a key that is equal to the original key.
     *
     * @param      key     the AppContext key.
     * @param      value   the value.
     * @return     the previous value of the specified key in this
     *             AppContext, or {@code null} if it did not have one.
     * @exception  NullPointerException  if the key or value is
     *               {@code null}.
     * @see     #get(Object)
     * @since   1.2
     */
    public Object put(Object key, Object value) {
	synchronized (table) {
	    MostRecentKeyValue recent = mostRecentKeyValue;
	    if ((recent != null) && (recent.key == key))
		recent.value = value;
	    return table.put(key, value);
	}
    }

    private final Map&lt;Object, Object&gt; table = new HashMap&lt;&gt;();
    private MostRecentKeyValue mostRecentKeyValue = null;

}

