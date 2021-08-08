class CipherFeedback extends FeedbackCipher {
    /**
     * Save the current content of this cipher.
     */
    void save() {
	if (registerSave == null) {
	    registerSave = new byte[blockSize];
	}
	System.arraycopy(register, 0, registerSave, 0, blockSize);
    }

    private byte[] registerSave = null;
    private final byte[] register;

}

