class Type implements Constants {
    /**
     * Check for a certain type.
     */
    public final boolean isType(int tc) {
	return typeCode == tc;
    }

    /**
     * The TypeCode of this type. The value of this field is one
     * of the TC_* contant values defined in Constants.
     * @see Constants
     */
    protected int typeCode;

}

