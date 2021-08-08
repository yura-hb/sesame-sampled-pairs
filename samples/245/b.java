class XMLNotationDecl {
    /**
     * setValues
     *
     * @param name
     * @param publicId
     * @param systemId
     */
    public void setValues(String name, String publicId, String systemId, String baseSystemId) {
	this.name = name;
	this.publicId = publicId;
	this.systemId = systemId;
	this.baseSystemId = baseSystemId;
    }

    /** name */
    public String name;
    /** publicId */
    public String publicId;
    /** systemId */
    public String systemId;
    /** base systemId */
    public String baseSystemId;

}

