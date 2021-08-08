import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Double.longBitsToDouble;
import java.util.concurrent.atomic.AtomicLongArray;

class AtomicDoubleArray implements Serializable {
    /**
    * Atomically adds the given value to the element at index {@code i}.
    *
    * @param i the index
    * @param delta the value to add
    * @return the previous value
    */
    @CanIgnoreReturnValue
    public final double getAndAdd(int i, double delta) {
	while (true) {
	    long current = longs.get(i);
	    double currentVal = longBitsToDouble(current);
	    double nextVal = currentVal + delta;
	    long next = doubleToRawLongBits(nextVal);
	    if (longs.compareAndSet(i, current, next)) {
		return currentVal;
	    }
	}
    }

    private transient AtomicLongArray longs;

}

