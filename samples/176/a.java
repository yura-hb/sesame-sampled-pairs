class DefaultKeyValue&lt;K, V&gt; extends AbstractKeyValue&lt;K, V&gt; {
    /**
     * Sets the value.
     *
     * @return the old value of the value
     * @param value the new value
     * @throws IllegalArgumentException if value is this object
     */
    @Override
    public V setValue(final V value) {
	if (value == this) {
	    throw new IllegalArgumentException("DefaultKeyValue may not contain itself as a value.");
	}

	return super.setValue(value);
    }

}

