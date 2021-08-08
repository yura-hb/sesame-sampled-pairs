class DOMValidateContext extends DOMCryptoContext implements XMLValidateContext {
    /**
     * Sets the node.
     *
     * @param node the node
     * @throws NullPointerException if &lt;code&gt;node&lt;/code&gt; is &lt;code&gt;null&lt;/code&gt;
     * @see #getNode
     */
    public void setNode(Node node) {
	if (node == null) {
	    throw new NullPointerException();
	}
	this.node = node;
    }

    private Node node;

}

