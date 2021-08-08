import javax.crypto.BadPaddingException;

class RSAPadding {
    /**
     * Unpad the padded block and return the data.
     */
    public byte[] unpad(byte[] padded) throws BadPaddingException {
	if (padded.length != paddedSize) {
	    throw new BadPaddingException("Decryption error." + "The padded array length (" + padded.length
		    + ") is not the specified padded size (" + paddedSize + ")");
	}
	switch (type) {
	case PAD_NONE:
	    return padded;
	case PAD_BLOCKTYPE_1:
	case PAD_BLOCKTYPE_2:
	    return unpadV15(padded);
	case PAD_OAEP_MGF1:
	    return unpadOAEP(padded);
	default:
	    throw new AssertionError();
	}
    }

    private final int paddedSize;
    private final int type;
    public static final int PAD_NONE = 3;
    public static final int PAD_BLOCKTYPE_1 = 1;
    public static final int PAD_BLOCKTYPE_2 = 2;
    public static final int PAD_OAEP_MGF1 = 4;
    private final int maxDataSize;
    private byte[] lHash;
    private MGF1 mgf;

    /**
     * PKCS#1 v1.5 unpadding (blocktype 1 (signature) and 2 (encryption)).
     *
     * Note that we want to make it a constant-time operation
     */
    private byte[] unpadV15(byte[] padded) throws BadPaddingException {
	int k = 0;
	boolean bp = false;

	if (padded[k++] != 0) {
	    bp = true;
	}
	if (padded[k++] != type) {
	    bp = true;
	}
	int p = 0;
	while (k &lt; padded.length) {
	    int b = padded[k++] & 0xff;
	    if ((b == 0) && (p == 0)) {
		p = k;
	    }
	    if ((k == padded.length) && (p == 0)) {
		bp = true;
	    }
	    if ((type == PAD_BLOCKTYPE_1) && (b != 0xff) && (p == 0)) {
		bp = true;
	    }
	}
	int n = padded.length - p;
	if (n &gt; maxDataSize) {
	    bp = true;
	}

	// copy useless padding array for a constant-time method
	byte[] padding = new byte[p];
	System.arraycopy(padded, 0, padding, 0, p);

	byte[] data = new byte[n];
	System.arraycopy(padded, p, data, 0, n);

	BadPaddingException bpe = new BadPaddingException("Decryption error");

	if (bp) {
	    throw bpe;
	} else {
	    return data;
	}
    }

    /**
     * PKCS#1 v2.1 OAEP unpadding (MGF1).
     */
    private byte[] unpadOAEP(byte[] padded) throws BadPaddingException {
	byte[] EM = padded;
	boolean bp = false;
	int hLen = lHash.length;

	if (EM[0] != 0) {
	    bp = true;
	}

	int seedStart = 1;
	int seedLen = hLen;

	int dbStart = hLen + 1;
	int dbLen = EM.length - dbStart;

	mgf.generateAndXor(EM, dbStart, dbLen, seedLen, EM, seedStart);
	mgf.generateAndXor(EM, seedStart, seedLen, dbLen, EM, dbStart);

	// verify lHash == lHash'
	for (int i = 0; i &lt; hLen; i++) {
	    if (lHash[i] != EM[dbStart + i]) {
		bp = true;
	    }
	}

	int padStart = dbStart + hLen;
	int onePos = -1;

	for (int i = padStart; i &lt; EM.length; i++) {
	    int value = EM[i];
	    if (onePos == -1) {
		if (value == 0x00) {
		    // continue;
		} else if (value == 0x01) {
		    onePos = i;
		} else { // Anything other than {0,1} is bad.
		    bp = true;
		}
	    }
	}

	// We either ran off the rails or found something other than 0/1.
	if (onePos == -1) {
	    bp = true;
	    onePos = EM.length - 1; // Don't inadvertently return any data.
	}

	int mStart = onePos + 1;

	// copy useless padding array for a constant-time method
	byte[] tmp = new byte[mStart - padStart];
	System.arraycopy(EM, padStart, tmp, 0, tmp.length);

	byte[] m = new byte[EM.length - mStart];
	System.arraycopy(EM, mStart, m, 0, m.length);

	BadPaddingException bpe = new BadPaddingException("Decryption error");

	if (bp) {
	    throw bpe;
	} else {
	    return m;
	}
    }

}

