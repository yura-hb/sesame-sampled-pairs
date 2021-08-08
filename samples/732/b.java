import java.security.MessageDigest;

class SecureRandom extends SecureRandomSpi implements Serializable {
    /**
     * Generates a user-specified number of random bytes.
     *
     * @param result the array to be filled in with random bytes.
     */
    @Override
    public synchronized void engineNextBytes(byte[] result) {
	int index = 0;
	int todo;
	byte[] output = remainder;

	if (state == null) {
	    byte[] seed = new byte[DIGEST_SIZE];
	    SeederHolder.seeder.engineNextBytes(seed);
	    state = digest.digest(seed);
	}

	// Use remainder from last time
	int r = remCount;
	if (r &gt; 0) {
	    // How many bytes?
	    todo = (result.length - index) &lt; (DIGEST_SIZE - r) ? (result.length - index) : (DIGEST_SIZE - r);
	    // Copy the bytes, zero the buffer
	    for (int i = 0; i &lt; todo; i++) {
		result[i] = output[r];
		output[r++] = 0;
	    }
	    remCount += todo;
	    index += todo;
	}

	// If we need more bytes, make them.
	while (index &lt; result.length) {
	    // Step the state
	    digest.update(state);
	    output = digest.digest();
	    updateState(state, output);

	    // How many bytes?
	    todo = (result.length - index) &gt; DIGEST_SIZE ? DIGEST_SIZE : result.length - index;
	    // Copy the bytes, zero the buffer
	    for (int i = 0; i &lt; todo; i++) {
		result[index++] = output[i];
		output[i] = 0;
	    }
	    remCount += todo;
	}

	// Store remainder for next time
	remainder = output;
	remCount %= DIGEST_SIZE;
    }

    private byte[] remainder;
    private byte[] state;
    private static final int DIGEST_SIZE = 20;
    private transient MessageDigest digest;
    private int remCount;

    private static void updateState(byte[] state, byte[] output) {
	int last = 1;
	int v;
	byte t;
	boolean zf = false;

	// state(n + 1) = (state(n) + output(n) + 1) % 2^160;
	for (int i = 0; i &lt; state.length; i++) {
	    // Add two bytes
	    v = (int) state[i] + (int) output[i] + last;
	    // Result is lower 8 bits
	    t = (byte) v;
	    // Store result. Check for state collision.
	    zf = zf | (state[i] != t);
	    state[i] = t;
	    // High 8 bits are carry. Store for next iteration.
	    last = v &gt;&gt; 8;
	}

	// Make sure at least one bit changes!
	if (!zf) {
	    state[0]++;
	}
    }

}

