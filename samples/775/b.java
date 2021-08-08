import java.io.ByteArrayInputStream;
import java.io.IOException;

class DerInputBuffer extends ByteArrayInputStream implements Cloneable {
    /**
     * Returns the bit string which takes up the rest of this buffer.
     * The bit string need not be byte-aligned.
     */
    BitArray getUnalignedBitString() throws IOException {
	if (pos &gt;= count)
	    return null;
	/*
	 * Just copy the data into an aligned, padded octet buffer,
	 * and consume the rest of the buffer.
	 */
	int len = available();
	int unusedBits = buf[pos] & 0xff;
	if (unusedBits &gt; 7) {
	    throw new IOException("Invalid value for unused bits: " + unusedBits);
	}
	byte[] bits = new byte[len - 1];
	// number of valid bits
	int length = (bits.length == 0) ? 0 : bits.length * 8 - unusedBits;

	System.arraycopy(buf, pos + 1, bits, 0, len - 1);

	BitArray bitArray = new BitArray(length, bits);
	pos = count;
	return bitArray;
    }

}

