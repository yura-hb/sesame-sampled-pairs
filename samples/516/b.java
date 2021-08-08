class AttrImpl extends NodeImpl implements Attr, TypeInfo {
    /** NON-DOM, for use by parser */
    public void setSpecified(boolean arg) {

	if (needsSyncData()) {
	    synchronizeData();
	}
	isSpecified(arg);

    }

}

