abstract class XMLKit {
    class Element implements Comparable&lt;Element&gt;, Iterable&lt;Object&gt; {
	/** Equivalent to Collections.reverse(this.asList()). */
	public void reverse() {
	    for (int i = 0, mid = size &gt;&gt; 1, j = size - 1; i &lt; mid; i++, j--) {
		Object p = parts[i];
		parts[i] = parts[j];
		parts[j] = p;
	    }
	}

	int size;
	Object[] parts;

    }

}

