import java.lang.invoke.VarHandle;

class AtomicIntegerArray implements Serializable {
    /**
     * Atomically adds the given value to the element at index {@code i},
     * with memory effects as specified by {@link VarHandle#getAndAdd}.
     *
     * @param i the index
     * @param delta the value to add
     * @return the updated value
     */
    public final int addAndGet(int i, int delta) {
	return (int) AA.getAndAdd(array, i, delta) + delta;
    }

    private static final VarHandle AA = MethodHandles.arrayElementVarHandle(int[].class);
    private final int[] array;

}

