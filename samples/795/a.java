abstract class AbstractByteHasher extends AbstractHasher {
    /** Updates this hasher with {@code len} bytes starting at {@code off} in the given buffer. */
    protected void update(byte[] b, int off, int len) {
	for (int i = off; i &lt; off + len; i++) {
	    update(b[i]);
	}
    }

    /** Updates this hasher with the given byte. */
    protected abstract void update(byte b);

}

