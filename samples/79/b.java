class RecordedClass extends RecordedObject {
    /**
     * Returns the modifiers of the class.
     * &lt;p&gt;
     * See {@link java.lang.reflect.Modifier}
     *
     * @return the modifiers
     *
     * @see Modifier
     */
    public int getModifiers() {
	return getTyped("modifiers", Integer.class, -1);
    }

}

