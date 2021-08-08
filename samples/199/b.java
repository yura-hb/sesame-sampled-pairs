import java.util.Arrays;

class BitVector implements Cloneable {
    /**
     * Resize the bit vector to accommodate the new length.
     * @param length Number of bits required.
     */
    public void resize(final long length) {
	final int need = (int) growthNeeded(length);

	if (bits.length != need) {
	    bits = Arrays.copyOf(bits, need);
	}

	final int shift = (int) (length & BITMASK);
	int slot = (int) (length &gt;&gt; BITSHIFT);

	if (shift != 0) {
	    bits[slot] &= (1L &lt;&lt; shift) - 1;
	    slot++;
	}

	for (; slot &lt; bits.length; slot++) {
	    bits[slot] = 0;
	}
    }

    /** Bit area. */
    private long[] bits;
    /** Mask for indexing. */
    private static final int BITMASK = BITSPERSLOT - 1;
    /** Shift for indexing. */
    private static final int BITSHIFT = 6;
    /** Growth quanta when resizing. */
    private static final int SLOTSQUANTA = 4;

    /**
     * Calculate the number of slots need for the specified length of bits
     * rounded to allocation quanta.
     * @param length Number of bits required.
     * @return Number of slots needed rounded to allocation quanta.
     */
    private static long growthNeeded(final long length) {
	return (slotsNeeded(length) + SLOTSQUANTA - 1) / SLOTSQUANTA * SLOTSQUANTA;
    }

    /**
     * Calculate the number of slots need for the specified length of bits.
     * @param length Number of bits required.
     * @return Number of slots needed.
     */
    private static long slotsNeeded(final long length) {
	return (length + BITMASK) &gt;&gt; BITSHIFT;
    }

}

