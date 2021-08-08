import java.lang.invoke.VarHandle;

class AtomicLongArray implements Serializable {
    /**
     * Atomically adds the given value to the element at index {@code i},
     * with memory effects as specified by {@link VarHandle#getAndAdd}.
     *
     * @param i the index
     * @param delta the value to add
     * @return the previous value
     */
    public final long getAndAdd(int i, long delta) {
	return (long) AA.getAndAdd(array, i, delta);
    }

    private static final VarHandle AA = MethodHandles.arrayElementVarHandle(long[].class);
    private final long[] array;

}

