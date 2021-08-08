class ContentModelState {
    /**
     * Return the content model that is relevant to the current state.
     */
    public ContentModel getModel() {
	ContentModel m = model;
	for (int i = 0; i &lt; value; i++) {
	    if (m.next != null) {
		m = m.next;
	    } else {
		return null;
	    }
	}
	return m;
    }

    ContentModel model;
    long value;

}

