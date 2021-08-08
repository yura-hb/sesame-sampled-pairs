class DocumentTypeImpl extends ParentNode implements DocumentType {
    /**
     * NON-DOM: Access the collection of ElementDefinitions.
     * @see ElementDefinitionImpl
     */
    public NamedNodeMap getElements() {
	if (needsSyncChildren()) {
	    synchronizeChildren();
	}
	return elements;
    }

    /** Elements. */
    protected NamedNodeMapImpl elements;

}

