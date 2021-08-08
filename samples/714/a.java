abstract class MultimapBuilder&lt;K0, V0&gt; {
    abstract class MultimapBuilderWithKeys&lt;K0&gt; {
	/**
	* Uses a hash-based {@code Set} to store value collections, initialized to expect the specified number
	* of values per key.
	*
	* @throws IllegalArgumentException if {@code expectedValuesPerKey &lt; 0}
	*/
	public SetMultimapBuilder&lt;K0, Object&gt; hashSetValues(final int expectedValuesPerKey) {
	    checkNonnegative(expectedValuesPerKey, "expectedValuesPerKey");
	    return new SetMultimapBuilder&lt;K0, Object&gt;() {
		@Override
		public &lt;K extends K0, V&gt; SetMultimap&lt;K, V&gt; build() {
		    return Multimaps.newSetMultimap(MultimapBuilderWithKeys.this.&lt;K, V&gt;createMap(),
			    new HashSetSupplier&lt;V&gt;(expectedValuesPerKey));
		}
	    };
	}

	abstract &lt;K extends K0, V&gt; Map&lt;K, Collection&lt;V&gt;&gt; createMap();

    }

    class HashSetSupplier&lt;V&gt; implements Supplier&lt;Set&lt;V&gt;&gt;, Serializable {
	HashSetSupplier(int expectedValuesPerKey) {
	    this.expectedValuesPerKey = checkNonnegative(expectedValuesPerKey, "expectedValuesPerKey");
	}

    }

}

