import jdk.internal.vm.compiler.collections.EconomicMap;

class FrequencyEncoder&lt;T&gt; {
    /**
     * Adds an object to the array.
     */
    public void addObject(T object) {
	if (object == null) {
	    containsNull = true;
	    return;
	}

	Entry&lt;T&gt; entry = map.get(object);
	if (entry == null) {
	    entry = new Entry&lt;&gt;(object);
	    map.put(object, entry);
	}
	entry.frequency++;
    }

    protected boolean containsNull;
    protected final EconomicMap&lt;T, Entry&lt;T&gt;&gt; map;

    class Entry&lt;T&gt; {
	protected boolean containsNull;
	protected final EconomicMap&lt;T, Entry&lt;T&gt;&gt; map;

	protected Entry(T object) {
	    this.object = object;
	    this.index = -1;
	}

    }

}

