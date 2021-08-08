import java.lang.invoke.VarHandle;

abstract class Striped64 extends Number {
    /**
     * Pseudo-randomly advances and records the given probe value for the
     * given thread.
     * Duplicated from ThreadLocalRandom because of packaging restrictions.
     */
    static final int advanceProbe(int probe) {
	probe ^= probe &lt;&lt; 13; // xorshift
	probe ^= probe &gt;&gt;&gt; 17;
	probe ^= probe &lt;&lt; 5;
	THREAD_PROBE.set(Thread.currentThread(), probe);
	return probe;
    }

    private static final VarHandle THREAD_PROBE;

}

