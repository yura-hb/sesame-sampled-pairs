class JDKXPathAPI implements XPathAPI {
    /**
     * Clear any context information from this object
     */
    public void clear() {
	xpathStr = null;
	xpathExpression = null;
	xpf = null;
    }

    private String xpathStr;
    private XPathExpression xpathExpression;
    private XPathFactory xpf;

}

