import java.util.Map;

abstract class ASTNode {
    /**
     * Returns the value of the named property of this node, or &lt;code&gt;null&lt;/code&gt; if none.
     *
     * @param propertyName the property name
     * @return the property value, or &lt;code&gt;null&lt;/code&gt; if none
     * @see #setProperty(String,Object)
     */
    public final Object getProperty(String propertyName) {
	if (propertyName == null) {
	    throw new IllegalArgumentException();
	}
	if (this.property1 == null) {
	    // node has no properties at all
	    return null;
	}
	if (this.property1 instanceof String) {
	    // node has only a single property
	    if (propertyName.equals(this.property1)) {
		return this.property2;
	    } else {
		return null;
	    }
	}
	// otherwise node has table of properties
	Map m = (Map) this.property1;
	return m.get(propertyName);
    }

    /**
     * Primary field used in representing node properties efficiently.
     * If &lt;code&gt;null&lt;/code&gt;, this node has no properties.
     * If a {@link String}, this is the name of this node's sole property,
     * and &lt;code&gt;property2&lt;/code&gt; contains its value.
     * If a {@link Map}, this is the table of property name-value
     * mappings; &lt;code&gt;property2&lt;/code&gt;, if non-null is its unmodifiable
     * equivalent.
     * Initially &lt;code&gt;null&lt;/code&gt;.
     *
     * @see #property2
     */
    private Object property1 = null;
    /**
     * Auxiliary field used in representing node properties efficiently.
     *
     * @see #property1
     */
    private Object property2 = null;

}

