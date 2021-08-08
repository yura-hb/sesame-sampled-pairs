import com.github.benmanes.caffeine.base.UnsafeAccess;

abstract class StripedBuffer&lt;E&gt; implements Buffer&lt;E&gt; {
    /** CASes the tableBusy field from 0 to 1 to acquire lock. */
    final boolean casTableBusy() {
	return UnsafeAccess.UNSAFE.compareAndSwapInt(this, TABLE_BUSY, 0, 1);
    }

    static final long TABLE_BUSY = UnsafeAccess.objectFieldOffset(StripedBuffer.class, "tableBusy");

}

