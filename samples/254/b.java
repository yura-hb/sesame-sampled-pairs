import java.util.*;

abstract class SHA3 extends DigestBase {
    /**
     * Resets the internal state to start a new hash.
     */
    void implReset() {
	Arrays.fill(state, (byte) 0);
	Arrays.fill(lanes, 0L);
    }

    private byte[] state = new byte[WIDTH];
    private final long[] lanes = new long[DM * DM];

}

