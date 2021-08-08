import java.util.NoSuchElementException;

class Optional&lt;T&gt; {
    /**
     * Return the value if present, otherwise return other.
     *
     * @param other  the value to be returned if there is no value present, may be null
     * @return
     */
    public T orElse(T other) {
	if (isPresent()) {
	    return get();
	}
	return other;
    }

    private final T value;

    /**
     * Return true if there is a value present, otherwise false.
     *
     * @return true if there is a value present, otherwise false
     */
    public boolean isPresent() {
	return value != null;
    }

    /**
     * If a value is present in this Optional, returns the value, otherwise throws NoSuchElementException.
     *
     * @return the non-null value held by this Optional
     * @throws NoSuchElementException - if there is no value present
     */
    public T get() {
	if (!isPresent()) {
	    throw new NoSuchElementException("Optional is empty");
	}
	return value;
    }

}

