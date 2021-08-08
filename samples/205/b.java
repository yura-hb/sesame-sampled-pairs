class DoubleJVMOption extends JVMOption {
    /**
     * Set new maximum option value
     *
     * @param max new maximum value
     */
    @Override
    void setMax(String max) {
	this.max = new Double(max);
    }

    /**
     * Maximum option value
     */
    private double max;

}

