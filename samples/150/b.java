import java.util.*;

class Util {
    /**
     * Returns a Set containing the given elements.
     */
    @SafeVarargs
    static &lt;E&gt; Set&lt;E&gt; newSet(E... elements) {
	HashSet&lt;E&gt; set = new HashSet&lt;&gt;();
	for (E e : elements) {
	    set.add(e);
	}
	return set;
    }

}

