import java.lang.invoke.VarHandle;

abstract class Striped64 extends Number {
    /**
     * Returns the probe value for the current thread.
     * Duplicated from ThreadLocalRandom because of packaging restrictions.
     */
    static final int getProbe() {
	return (int) THREAD_PROBE.get(Thread.currentThread());
    }

    private static final VarHandle THREAD_PROBE;

}

