import java.util.Iterator;

class IteratorUtils {
    /**
     * Returns a string representation of the elements of the specified iterator.
     * &lt;p&gt;
     * The string representation consists of a list of the iterator's elements,
     * enclosed in square brackets ({@code "[]"}). Adjacent elements are separated
     * by the characters {@code ", "} (a comma followed by a space). Elements are
     * converted to strings as by {@code String.valueOf(Object)}.
     *
     * @param &lt;E&gt; the element type
     * @param iterator  the iterator to convert to a string, may be null
     * @return a string representation of {@code iterator}
     * @since 4.1
     */
    public static &lt;E&gt; String toString(final Iterator&lt;E&gt; iterator) {
	return toString(iterator, TransformerUtils.stringValueTransformer(), DEFAULT_TOSTRING_DELIMITER,
		DEFAULT_TOSTRING_PREFIX, DEFAULT_TOSTRING_SUFFIX);
    }

    /**
     * Default delimiter used to delimit elements while converting an Iterator
     * to its String representation.
     */
    private static final String DEFAULT_TOSTRING_DELIMITER = ", ";
    /**
     * Default prefix used while converting an Iterator to its String representation.
     */
    private static final String DEFAULT_TOSTRING_PREFIX = "[";
    /**
     * Default suffix used while converting an Iterator to its String representation.
     */
    private static final String DEFAULT_TOSTRING_SUFFIX = "]";

    /**
     * Returns a string representation of the elements of the specified iterator.
     * &lt;p&gt;
     * The string representation consists of a list of the iterator's elements,
     * enclosed by the provided {@code prefix} and {@code suffix}. Adjacent elements
     * are separated by the provided {@code delimiter}. Elements are converted to
     * strings as by using the provided {@code transformer}.
     *
     * @param &lt;E&gt; the element type
     * @param iterator  the iterator to convert to a string, may be null
     * @param transformer  the transformer used to get a string representation of an element
     * @param delimiter  the string to delimit elements
     * @param prefix  the prefix, prepended to the string representation
     * @param suffix  the suffix, appended to the string representation
     * @return a string representation of {@code iterator}
     * @throws NullPointerException if either transformer, delimiter, prefix or suffix is null
     * @since 4.1
     */
    public static &lt;E&gt; String toString(final Iterator&lt;E&gt; iterator, final Transformer&lt;? super E, String&gt; transformer,
	    final String delimiter, final String prefix, final String suffix) {
	if (transformer == null) {
	    throw new NullPointerException("transformer may not be null");
	}
	if (delimiter == null) {
	    throw new NullPointerException("delimiter may not be null");
	}
	if (prefix == null) {
	    throw new NullPointerException("prefix may not be null");
	}
	if (suffix == null) {
	    throw new NullPointerException("suffix may not be null");
	}
	final StringBuilder stringBuilder = new StringBuilder(prefix);
	if (iterator != null) {
	    while (iterator.hasNext()) {
		final E element = iterator.next();
		stringBuilder.append(transformer.transform(element));
		stringBuilder.append(delimiter);
	    }
	    if (stringBuilder.length() &gt; prefix.length()) {
		stringBuilder.setLength(stringBuilder.length() - delimiter.length());
	    }
	}
	stringBuilder.append(suffix);
	return stringBuilder.toString();
    }

}

