import com.github.benmanes.caffeine.base.UnsafeAccess;

abstract class StripedBuffer&lt;E&gt; implements Buffer&lt;E&gt; {
    /**
    * Pseudo-randomly advances and records the given probe value for the given thread. Duplicated
    * from ThreadLocalRandom because of packaging restrictions.
    */
    static final int advanceProbe(int probe) {
	probe ^= probe &lt;&lt; 13; // xorshift
	probe ^= probe &gt;&gt;&gt; 17;
	probe ^= probe &lt;&lt; 5;
	UnsafeAccess.UNSAFE.putInt(Thread.currentThread(), PROBE, probe);
	return probe;
    }

    static final long PROBE = UnsafeAccess.objectFieldOffset(Thread.class, "threadLocalRandomProbe");

}

