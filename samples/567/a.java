class DefaultKeyValue&lt;K, V&gt; extends AbstractKeyValue&lt;K, V&gt; {
    /**
     * Sets the key.
     *
     * @param key  the new key
     * @return the old key
     * @throws IllegalArgumentException if key is this object
     */
    @Override
    public K setKey(final K key) {
	if (key == this) {
	    throw new IllegalArgumentException("DefaultKeyValue may not contain itself as a key.");
	}

	return super.setKey(key);
    }

}

