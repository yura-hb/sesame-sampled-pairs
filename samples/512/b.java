class DocumentTypeImpl extends ParentNode implements DocumentType {
    /**
     * Returns the document type name
     */
    public String getNodeName() {
	if (needsSyncData()) {
	    synchronizeData();
	}
	return name;
    }

    /** Document type name. */
    protected String name;

}

