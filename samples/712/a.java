abstract class TypeToken&lt;T&gt; extends TypeCapture&lt;T&gt; implements Serializable {
    /**
    * Resolves the given {@code type} against the type context represented by this type. For example:
    *
    * &lt;pre&gt;{@code
    * new TypeToken&lt;List&lt;String&gt;&gt;() {}.resolveType(
    *     List.class.getMethod("get", int.class).getGenericReturnType())
    * =&gt; String.class
    * }&lt;/pre&gt;
    */
    public final TypeToken&lt;?&gt; resolveType(Type type) {
	checkNotNull(type);
	// Being conservative here because the user could use resolveType() to resolve a type in an
	// invariant context.
	return of(getInvariantTypeResolver().resolveType(type));
    }

    /** Resolver for resolving parameter and field types with {@link #runtimeType} as context. */
    private transient @MonotonicNonNull TypeResolver invariantTypeResolver;
    private final Type runtimeType;

    private TypeResolver getInvariantTypeResolver() {
	TypeResolver resolver = invariantTypeResolver;
	if (resolver == null) {
	    resolver = (invariantTypeResolver = TypeResolver.invariantly(runtimeType));
	}
	return resolver;
    }

    /** Returns an instance of type token that wraps {@code type}. */
    public static TypeToken&lt;?&gt; of(Type type) {
	return new SimpleTypeToken&lt;&gt;(type);
    }

    private TypeToken(Type type) {
	this.runtimeType = checkNotNull(type);
    }

    class SimpleTypeToken&lt;T&gt; extends TypeToken&lt;T&gt; {
	/** Resolver for resolving parameter and field types with {@link #runtimeType} as context. */
	private transient @MonotonicNonNull TypeResolver invariantTypeResolver;
	private final Type runtimeType;

	SimpleTypeToken(Type type) {
	    super(type);
	}

    }

}

