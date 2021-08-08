class KerasLayer {
    /**
     * Whether this Keras layer maps to a DL4J Vertex.
     *
     * @return true or false
     */
    public boolean isVertex() {
	return this.vertex != null;
    }

    protected GraphVertex vertex;

}

