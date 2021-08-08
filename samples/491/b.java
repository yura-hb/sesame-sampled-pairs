import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

class ConcurrentHashMultiset&lt;E&gt; extends AbstractMultiset&lt;E&gt; implements Serializable {
    /**
    * Adds or removes occurrences of {@code element} such that the {@link #count} of the element
    * becomes {@code count}.
    *
    * @return the count of {@code element} in the multiset before this call
    * @throws IllegalArgumentException if {@code count} is negative
    */
    @CanIgnoreReturnValue
    @Override
    public int setCount(E element, int count) {
	checkNotNull(element);
	checkNonnegative(count, "count");
	while (true) {
	    AtomicInteger existingCounter = Maps.safeGet(countMap, element);
	    if (existingCounter == null) {
		if (count == 0) {
		    return 0;
		} else {
		    existingCounter = countMap.putIfAbsent(element, new AtomicInteger(count));
		    if (existingCounter == null) {
			return 0;
		    }
		    // existingCounter != null: fall through
		}
	    }

	    while (true) {
		int oldValue = existingCounter.get();
		if (oldValue == 0) {
		    if (count == 0) {
			return 0;
		    } else {
			AtomicInteger newCounter = new AtomicInteger(count);
			if ((countMap.putIfAbsent(element, newCounter) == null)
				|| countMap.replace(element, existingCounter, newCounter)) {
			    return 0;
			}
		    }
		    break;
		} else {
		    if (existingCounter.compareAndSet(oldValue, count)) {
			if (count == 0) {
			    // Just CASed to 0; remove the entry to clean up the map. If the removal fails,
			    // another thread has already replaced it with a new counter, which is fine.
			    countMap.remove(element, existingCounter);
			}
			return oldValue;
		    }
		}
	    }
	}
    }

    /** The number of occurrences of each element. */
    private final transient ConcurrentMap&lt;E, AtomicInteger&gt; countMap;

}

