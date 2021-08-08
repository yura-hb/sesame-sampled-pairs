import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

class Frequency&lt;T&gt; implements Serializable {
    /**
     * Returns the mode value(s) in comparator order.
     *
     * @return a list containing the value(s) which appear most often.
     * @since 3.3
     */
    public List&lt;T&gt; getMode() {
	long mostPopular = 0; // frequencies are always positive

	// Get the max count first, so we avoid having to recreate the List each time
	for (Long l : freqTable.values()) {
	    long frequency = l.longValue();
	    if (frequency &gt; mostPopular) {
		mostPopular = frequency;
	    }
	}

	List&lt;T&gt; modeList = new ArrayList&lt;&gt;();
	for (Entry&lt;T, Long&gt; ent : freqTable.entrySet()) {
	    long frequency = ent.getValue().longValue();
	    if (frequency == mostPopular) {
		modeList.add(ent.getKey());
	    }
	}
	return modeList;
    }

    /** underlying collection */
    private final SortedMap&lt;T, Long&gt; freqTable;

}

