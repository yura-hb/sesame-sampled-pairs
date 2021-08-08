import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Double.longBitsToDouble;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

class AtomicDouble extends Number implements Serializable {
    /**
    * Atomically adds the given value to the current value.
    *
    * @param delta the value to add
    * @return the updated value
    */
    @CanIgnoreReturnValue
    public final double addAndGet(double delta) {
	while (true) {
	    long current = value;
	    double currentVal = longBitsToDouble(current);
	    double nextVal = currentVal + delta;
	    long next = doubleToRawLongBits(nextVal);
	    if (updater.compareAndSet(this, current, next)) {
		return nextVal;
	    }
	}
    }

    private transient volatile long value;
    private static final AtomicLongFieldUpdater&lt;AtomicDouble&gt; updater = AtomicLongFieldUpdater
	    .newUpdater(AtomicDouble.class, "value");

}

