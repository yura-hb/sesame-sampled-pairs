class MnistManager {
    /**
     * Reads the current label.
     *
     * @return int
     * @throws java.io.IOException
     */
    public int readLabel() throws IOException {
	if (labels == null) {
	    throw new IllegalStateException("labels file not initialized.");
	}
	return labels.readLabel();
    }

    private MnistLabelFile labels;

}

