import java.util.*;

class T4241573 {
    /** Return the difference of two sets, a - b. */
    &lt;T&gt; Set&lt;T&gt; diff(Set&lt;T&gt; a, Set&lt;T&gt; b) {
	if (b.isEmpty())
	    return a;
	Set&lt;T&gt; result = new LinkedHashSet&lt;T&gt;(a);
	result.removeAll(b);
	return result;
    }

}

