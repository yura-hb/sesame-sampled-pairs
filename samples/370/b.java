import java.util.Iterator;
import java.util.Set;

class Sets {
    /** Remove each element in an iterable from a set. */
    static boolean removeAllImpl(Set&lt;?&gt; set, Iterator&lt;?&gt; iterator) {
	boolean changed = false;
	while (iterator.hasNext()) {
	    changed |= set.remove(iterator.next());
	}
	return changed;
    }

}

