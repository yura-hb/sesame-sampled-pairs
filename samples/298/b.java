import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class Util {
    /**
     * Convenience method to create a set with strings.
     */
    public static Set&lt;String&gt; set(String... ss) {
	Set&lt;String&gt; set = new HashSet&lt;&gt;();
	set.addAll(Arrays.asList(ss));
	return set;
    }

}

