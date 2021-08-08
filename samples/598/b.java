import static java.lang.Integer.reverseBytes;
import jdk.internal.misc.Unsafe;

class ByteArrayAccess {
    /**
     * int[] to byte[] conversion, little endian byte order.
     */
    static void i2bLittle(int[] in, int inOfs, byte[] out, int outOfs, int len) {
	if ((inOfs &lt; 0) || ((in.length - inOfs) &lt; len / 4) || (outOfs &lt; 0) || ((out.length - outOfs) &lt; len)) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	if (littleEndianUnaligned) {
	    outOfs += byteArrayOfs;
	    len += outOfs;
	    while (outOfs &lt; len) {
		unsafe.putInt(out, (long) outOfs, in[inOfs++]);
		outOfs += 4;
	    }
	} else if (bigEndian && ((outOfs & 3) == 0)) {
	    outOfs += byteArrayOfs;
	    len += outOfs;
	    while (outOfs &lt; len) {
		unsafe.putInt(out, (long) outOfs, reverseBytes(in[inOfs++]));
		outOfs += 4;
	    }
	} else {
	    len += outOfs;
	    while (outOfs &lt; len) {
		int i = in[inOfs++];
		out[outOfs++] = (byte) (i);
		out[outOfs++] = (byte) (i &gt;&gt; 8);
		out[outOfs++] = (byte) (i &gt;&gt; 16);
		out[outOfs++] = (byte) (i &gt;&gt; 24);
	    }
	}
    }

    private static final boolean littleEndianUnaligned;
    private static final int byteArrayOfs = unsafe.arrayBaseOffset(byte[].class);
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final boolean bigEndian;

}

