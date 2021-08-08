import com.github.benmanes.caffeine.base.UnsafeAccess;

abstract class StripedBuffer&lt;E&gt; implements Buffer&lt;E&gt; {
    /**
    * Returns the probe value for the current thread. Duplicated from ThreadLocalRandom because of
    * packaging restrictions.
    */
    static final int getProbe() {
	return UnsafeAccess.UNSAFE.getInt(Thread.currentThread(), PROBE);
    }

    static final long PROBE = UnsafeAccess.objectFieldOffset(Thread.class, "threadLocalRandomProbe");

}

