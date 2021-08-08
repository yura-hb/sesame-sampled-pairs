import java.util.LinkedHashMap;

class HashBasedTable&lt;R, C, V&gt; extends StandardTable&lt;R, C, V&gt; {
    /**
    * Creates a {@code HashBasedTable} with the same mappings as the specified table.
    *
    * @param table the table to copy
    * @throws NullPointerException if any of the row keys, column keys, or values in {@code table} is
    *     null
    */
    public static &lt;R, C, V&gt; HashBasedTable&lt;R, C, V&gt; create(Table&lt;? extends R, ? extends C, ? extends V&gt; table) {
	HashBasedTable&lt;R, C, V&gt; result = create();
	result.putAll(table);
	return result;
    }

    /** Creates an empty {@code HashBasedTable}. */
    public static &lt;R, C, V&gt; HashBasedTable&lt;R, C, V&gt; create() {
	return new HashBasedTable&lt;&gt;(new LinkedHashMap&lt;R, Map&lt;C, V&gt;&gt;(), new Factory&lt;C, V&gt;(0));
    }

    HashBasedTable(Map&lt;R, Map&lt;C, V&gt;&gt; backingMap, Factory&lt;C, V&gt; factory) {
	super(backingMap, factory);
    }

    class Factory&lt;C, V&gt; implements Supplier&lt;Map&lt;C, V&gt;&gt;, Serializable {
	Factory(int expectedSize) {
	    this.expectedSize = expectedSize;
	}

    }

}

