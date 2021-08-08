import com.google.common.primitives.Longs;
import java.io.FilterOutputStream;
import java.io.OutputStream;

class LittleEndianDataOutputStream extends FilterOutputStream implements DataOutput {
    /**
    * Writes a {@code long} as specified by {@link DataOutputStream#writeLong(long)}, except using
    * little-endian byte order.
    *
    * @throws IOException if an I/O error occurs
    */
    @Override
    public void writeLong(long v) throws IOException {
	byte[] bytes = Longs.toByteArray(Long.reverseBytes(v));
	write(bytes, 0, bytes.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
	// Override slow FilterOutputStream impl
	out.write(b, off, len);
    }

}

