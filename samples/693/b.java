class Wrapper extends Enum&lt;Wrapper&gt; {
    /** If {@code type} is a wrapper type, return the corresponding
     *  primitive type, else return {@code type} unchanged.
     */
    public static &lt;T&gt; Class&lt;T&gt; asPrimitiveType(Class&lt;T&gt; type) {
	Wrapper w = findWrapperType(type);
	if (w != null) {
	    return forceType(w.primitiveType(), type);
	}
	return type;
    }

    private static final Wrapper[] FROM_WRAP = new Wrapper[16];
    private final Class&lt;?&gt; wrapperType;
    private final Class&lt;?&gt; primitiveType;
    private static final Wrapper[] FROM_PRIM = new Wrapper[16];

    static Wrapper findWrapperType(Class&lt;?&gt; type) {
	Wrapper w = FROM_WRAP[hashWrap(type)];
	if (w != null && w.wrapperType == type) {
	    return w;
	}
	return null;
    }

    /** What is the primitive type wrapped by this wrapper? */
    public Class&lt;?&gt; primitiveType() {
	return primitiveType;
    }

    /** Cast a reference type to another reference type.
     * If the target type is an interface, perform no runtime check.
     * (This loophole is safe, and is allowed by the JVM verifier.)
     * If the target type is a primitive, change it to a wrapper.
     */
    static &lt;T&gt; Class&lt;T&gt; forceType(Class&lt;?&gt; type, Class&lt;T&gt; exampleType) {
	assert (type == exampleType || type.isPrimitive() && forPrimitiveType(type) == findWrapperType(exampleType)
		|| exampleType.isPrimitive() && forPrimitiveType(exampleType) == findWrapperType(type)
		|| type == Object.class && !exampleType.isPrimitive());
	@SuppressWarnings("unchecked")
	Class&lt;T&gt; result = (Class&lt;T&gt;) type; // unchecked warning is expected here
	return result;
    }

    private static int hashWrap(Class&lt;?&gt; x) {
	String xn = x.getName();
	final int offset = 10;
	assert (offset == "java.lang.".length());
	if (xn.length() &lt; offset + 3)
	    return 0;
	return (3 * xn.charAt(offset + 1) + xn.charAt(offset + 2)) % 16;
    }

    /** Return the wrapper that wraps values of the given type.
     *  The type may be {@code Object}, meaning the {@code OBJECT} wrapper.
     *  Otherwise, the type must be a primitive.
     *  @throws IllegalArgumentException for unexpected types
     */
    public static Wrapper forPrimitiveType(Class&lt;?&gt; type) {
	Wrapper w = findPrimitiveType(type);
	if (w != null)
	    return w;
	if (type.isPrimitive())
	    throw new InternalError(); // redo hash function
	throw newIllegalArgumentException("not primitive: " + type);
    }

    static Wrapper findPrimitiveType(Class&lt;?&gt; type) {
	Wrapper w = FROM_PRIM[hashPrim(type)];
	if (w != null && w.primitiveType == type) {
	    return w;
	}
	return null;
    }

    private static RuntimeException newIllegalArgumentException(String message) {
	return new IllegalArgumentException(message);
    }

    private static int hashPrim(Class&lt;?&gt; x) {
	String xn = x.getName();
	if (xn.length() &lt; 3)
	    return 0;
	return (xn.charAt(0) + xn.charAt(2)) % 16;
    }

}

