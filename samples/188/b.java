import gnu.trove.impl.hash.TObjectHash;

class THashMap&lt;K, V&gt; extends TObjectHash&lt;K&gt; implements TMap&lt;K, V&gt;, Externalizable {
    /**
     * checks for the presence of &lt;tt&gt;val&lt;/tt&gt; in the values of the map.
     *
     * @param val an &lt;code&gt;Object&lt;/code&gt; value
     * @return a &lt;code&gt;boolean&lt;/code&gt; value
     */
    public boolean containsValue(Object val) {
	Object[] set = _set;
	V[] vals = _values;

	// special case null values so that we don't have to
	// perform null checks before every call to equals()
	if (null == val) {
	    for (int i = vals.length; i-- &gt; 0;) {
		if ((set[i] != FREE && set[i] != REMOVED) && val == vals[i]) {
		    return true;
		}
	    }
	} else {
	    for (int i = vals.length; i-- &gt; 0;) {
		if ((set[i] != FREE && set[i] != REMOVED) && (val == vals[i] || equals(val, vals[i]))) {
		    return true;
		}
	    }
	} // end of else
	return false;
    }

    /**
     * the values of the  map
     */
    protected transient V[] _values;

}

