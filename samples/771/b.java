class AttrImpl extends NodeImpl implements Attr, TypeInfo {
    /**
     * Returns the attribute name
     */
    public String getNodeName() {
	if (needsSyncData()) {
	    synchronizeData();
	}
	return name;
    }

    /** Attribute name. */
    protected String name;

}

