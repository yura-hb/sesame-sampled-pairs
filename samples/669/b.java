class HashFunctions {
    /**
     * Returns a hashcode for the specified object.
     *
     * @return  a hash code value for the specified object.
     */
    public static int hash(Object object) {
	return object == null ? 0 : object.hashCode();
    }

}

