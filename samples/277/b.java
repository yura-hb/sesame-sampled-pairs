class Root {
    /**
     * Given two Root instances, return the one that is most interesting.
     */
    public Root mostInteresting(Root other) {
	if (other.type &gt; this.type) {
	    return other;
	} else {
	    return this;
	}
    }

    private int type;

}

