import jdk.internal.ref.Cleaner;
import sun.nio.ch.DirectBuffer;

class Unsafe {
    /**
     * Invokes the given direct byte buffer's cleaner, if any.
     *
     * @param directBuffer a direct byte buffer
     * @throws NullPointerException if {@code directBuffer} is null
     * @throws IllegalArgumentException if {@code directBuffer} is non-direct,
     * or is a {@link java.nio.Buffer#slice slice}, or is a
     * {@link java.nio.Buffer#duplicate duplicate}
     * @since 9
     */
    public void invokeCleaner(java.nio.ByteBuffer directBuffer) {
	if (!directBuffer.isDirect())
	    throw new IllegalArgumentException("buffer is non-direct");

	DirectBuffer db = (DirectBuffer) directBuffer;
	if (db.attachment() != null)
	    throw new IllegalArgumentException("duplicate or slice");

	Cleaner cleaner = db.cleaner();
	if (cleaner != null) {
	    cleaner.clean();
	}
    }

}

