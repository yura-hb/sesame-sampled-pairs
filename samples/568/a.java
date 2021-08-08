class MnistManager {
    /**
     * Set the position to be read.
     *
     * @param index
     */
    public void setCurrent(int index) {
	images.setCurrentIndex(index);
	labels.setCurrentIndex(index);
    }

    MnistImageFile images;
    private MnistLabelFile labels;

}

