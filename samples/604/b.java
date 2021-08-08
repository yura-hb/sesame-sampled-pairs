import javax.crypto.*;

class CipherCore {
    /**
     * Sets the padding mechanism of this cipher.
     *
     * @param padding the padding mechanism
     *
     * @exception NoSuchPaddingException if the requested padding mechanism
     * does not exist
     */
    void setPadding(String paddingScheme) throws NoSuchPaddingException {
	if (paddingScheme == null) {
	    throw new NoSuchPaddingException("null padding");
	}
	if (paddingScheme.equalsIgnoreCase("NoPadding")) {
	    padding = null;
	} else if (paddingScheme.equalsIgnoreCase("ISO10126Padding")) {
	    padding = new ISO10126Padding(blockSize);
	} else if (!paddingScheme.equalsIgnoreCase("PKCS5Padding")) {
	    throw new NoSuchPaddingException("Padding: " + paddingScheme + " not implemented");
	}
	if ((padding != null) && ((cipherMode == CTR_MODE) || (cipherMode == CTS_MODE) || (cipherMode == GCM_MODE))) {
	    padding = null;
	    String modeStr = null;
	    switch (cipherMode) {
	    case CTR_MODE:
		modeStr = "CTR";
		break;
	    case GCM_MODE:
		modeStr = "GCM";
		break;
	    case CTS_MODE:
		modeStr = "CTS";
		break;
	    default:
		// should never happen
	    }
	    if (modeStr != null) {
		throw new NoSuchPaddingException(modeStr + " mode must be used with NoPadding");
	    }
	}
    }

    private Padding padding = null;
    private int blockSize = 0;
    private int cipherMode = ECB_MODE;
    private static final int CTR_MODE = 5;
    private static final int CTS_MODE = 6;
    static final int GCM_MODE = 7;

}

