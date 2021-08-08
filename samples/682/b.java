import com.google.common.collect.ImmutableSet;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

abstract class TypeToken&lt;T&gt; extends TypeCapture&lt;T&gt; implements Serializable {
    /**
    * Returns the generic superclass of this type or {@code null} if the type represents {@link
    * Object} or an interface. This method is similar but different from {@link
    * Class#getGenericSuperclass}. For example, {@code new TypeToken&lt;StringArrayList&gt;()
    * {}.getGenericSuperclass()} will return {@code new TypeToken&lt;ArrayList&lt;String&gt;&gt;() {}}; while
    * {@code StringArrayList.class.getGenericSuperclass()} will return {@code ArrayList&lt;E&gt;}, where
    * {@code E} is the type variable declared by class {@code ArrayList}.
    *
    * &lt;p&gt;If this type is a type variable or wildcard, its first upper bound is examined and returned
    * if the bound is a class or extends from a class. This means that the returned type could be a
    * type variable too.
    */
    final @Nullable TypeToken&lt;? super T&gt; getGenericSuperclass() {
	if (runtimeType instanceof TypeVariable) {
	    // First bound is always the super class, if one exists.
	    return boundAsSuperclass(((TypeVariable&lt;?&gt;) runtimeType).getBounds()[0]);
	}
	if (runtimeType instanceof WildcardType) {
	    // wildcard has one and only one upper bound.
	    return boundAsSuperclass(((WildcardType) runtimeType).getUpperBounds()[0]);
	}
	Type superclass = getRawType().getGenericSuperclass();
	if (superclass == null) {
	    return null;
	}
	@SuppressWarnings("unchecked") // super class of T
	TypeToken&lt;? super T&gt; superToken = (TypeToken&lt;? super T&gt;) resolveSupertype(superclass);
	return superToken;
    }

    private final Type runtimeType;
    /** Resolver for resolving covariant types with {@link #runtimeType} as context. */
    private transient @MonotonicNonNull TypeResolver covariantTypeResolver;
    /** Resolver for resolving parameter and field types with {@link #runtimeType} as context. */
    private transient @MonotonicNonNull TypeResolver invariantTypeResolver;

    private @Nullable TypeToken&lt;? super T&gt; boundAsSuperclass(Type bound) {
	TypeToken&lt;?&gt; token = of(bound);
	if (token.getRawType().isInterface()) {
	    return null;
	}
	@SuppressWarnings("unchecked") // only upper bound of T is passed in.
	TypeToken&lt;? super T&gt; superclass = (TypeToken&lt;? super T&gt;) token;
	return superclass;
    }

    /**
    * Returns the raw type of {@code T}. Formally speaking, if {@code T} is returned by {@link
    * java.lang.reflect.Method#getGenericReturnType}, the raw type is what's returned by {@link
    * java.lang.reflect.Method#getReturnType} of the same method object. Specifically:
    *
    * &lt;ul&gt;
    *   &lt;li&gt;If {@code T} is a {@code Class} itself, {@code T} itself is returned.
    *   &lt;li&gt;If {@code T} is a {@link ParameterizedType}, the raw type of the parameterized type is
    *       returned.
    *   &lt;li&gt;If {@code T} is a {@link GenericArrayType}, the returned type is the corresponding array
    *       class. For example: {@code List&lt;Integer&gt;[] =&gt; List[]}.
    *   &lt;li&gt;If {@code T} is a type variable or a wildcard type, the raw type of the first upper bound
    *       is returned. For example: {@code &lt;X extends Foo&gt; =&gt; Foo}.
    * &lt;/ul&gt;
    */
    public final Class&lt;? super T&gt; getRawType() {
	// For wildcard or type variable, the first bound determines the runtime type.
	Class&lt;?&gt; rawType = getRawTypes().iterator().next();
	@SuppressWarnings("unchecked") // raw type is |T|
	Class&lt;? super T&gt; result = (Class&lt;? super T&gt;) rawType;
	return result;
    }

    private TypeToken&lt;?&gt; resolveSupertype(Type type) {
	TypeToken&lt;?&gt; supertype = of(getCovariantTypeResolver().resolveType(type));
	// super types' type mapping is a subset of type mapping of this type.
	supertype.covariantTypeResolver = covariantTypeResolver;
	supertype.invariantTypeResolver = invariantTypeResolver;
	return supertype;
    }

    /** Returns an instance of type token that wraps {@code type}. */
    public static TypeToken&lt;?&gt; of(Type type) {
	return new SimpleTypeToken&lt;&gt;(type);
    }

    private ImmutableSet&lt;Class&lt;? super T&gt;&gt; getRawTypes() {
	final ImmutableSet.Builder&lt;Class&lt;?&gt;&gt; builder = ImmutableSet.builder();
	new TypeVisitor() {
	    @Override
	    void visitTypeVariable(TypeVariable&lt;?&gt; t) {
		visit(t.getBounds());
	    }

	    @Override
	    void visitWildcardType(WildcardType t) {
		visit(t.getUpperBounds());
	    }

	    @Override
	    void visitParameterizedType(ParameterizedType t) {
		builder.add((Class&lt;?&gt;) t.getRawType());
	    }

	    @Override
	    void visitClass(Class&lt;?&gt; t) {
		builder.add(t);
	    }

	    @Override
	    void visitGenericArrayType(GenericArrayType t) {
		builder.add(Types.getArrayClass(of(t.getGenericComponentType()).getRawType()));
	    }
	}.visit(runtimeType);
	// Cast from ImmutableSet&lt;Class&lt;?&gt;&gt; to ImmutableSet&lt;Class&lt;? super T&gt;&gt;
	@SuppressWarnings({ "unchecked", "rawtypes" })
	ImmutableSet&lt;Class&lt;? super T&gt;&gt; result = (ImmutableSet) builder.build();
	return result;
    }

    private TypeResolver getCovariantTypeResolver() {
	TypeResolver resolver = covariantTypeResolver;
	if (resolver == null) {
	    resolver = (covariantTypeResolver = TypeResolver.covariantly(runtimeType));
	}
	return resolver;
    }

    private TypeToken(Type type) {
	this.runtimeType = checkNotNull(type);
    }

    class SimpleTypeToken&lt;T&gt; extends TypeToken&lt;T&gt; {
	private final Type runtimeType;
	/** Resolver for resolving covariant types with {@link #runtimeType} as context. */
	private transient @MonotonicNonNull TypeResolver covariantTypeResolver;
	/** Resolver for resolving parameter and field types with {@link #runtimeType} as context. */
	private transient @MonotonicNonNull TypeResolver invariantTypeResolver;

	SimpleTypeToken(Type type) {
	    super(type);
	}

    }

}

