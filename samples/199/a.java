import java.util.concurrent.atomic.AtomicLongArray;

class BloomFilterStrategies extends Enum&lt;BloomFilterStrategies&gt; implements Strategy {
    class LockFreeBitArray {
	/** Number of bits */
	long bitSize() {
	    return (long) data.length() * Long.SIZE;
	}

	final AtomicLongArray data;

    }

}

