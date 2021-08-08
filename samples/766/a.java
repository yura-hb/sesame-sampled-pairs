class CloudView {
    /**
     * Get the width in pixels rather than in width constant (like -1)
     */
    public int getRealWidth() {
	int width = getWidth();
	return (width &lt; 1) ? 1 : width;
    }

    protected MindMapCloud cloudModel;

    public int getWidth() {
	return getModel().getWidth();
    }

    protected MindMapCloud getModel() {
	return cloudModel;
    }

}

