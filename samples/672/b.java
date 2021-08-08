class UnmappableCharacterException extends CharacterCodingException {
    /**
     * Returns the message.
     * @return the message
     */
    public String getMessage() {
	return "Input length = " + inputLength;
    }

    private int inputLength;

}

