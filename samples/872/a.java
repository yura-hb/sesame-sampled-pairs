import com.google.common.base.Objects;
import java.util.List;

class Lists {
    /** An implementation of {@link List#equals(Object)}. */
    static boolean equalsImpl(List&lt;?&gt; thisList, @Nullable Object other) {
	if (other == checkNotNull(thisList)) {
	    return true;
	}
	if (!(other instanceof List)) {
	    return false;
	}
	List&lt;?&gt; otherList = (List&lt;?&gt;) other;
	int size = thisList.size();
	if (size != otherList.size()) {
	    return false;
	}
	if (thisList instanceof RandomAccess && otherList instanceof RandomAccess) {
	    // avoid allocation and use the faster loop
	    for (int i = 0; i &lt; size; i++) {
		if (!Objects.equal(thisList.get(i), otherList.get(i))) {
		    return false;
		}
	    }
	    return true;
	} else {
	    return Iterators.elementsEqual(thisList.iterator(), otherList.iterator());
	}
    }

}

