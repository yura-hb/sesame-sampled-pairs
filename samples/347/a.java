import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Comparator;
import java.util.Map;

abstract class TypeToken&lt;T&gt; extends TypeCapture&lt;T&gt; implements Serializable {
    class TypeSet extends ForwardingSet&lt;TypeToken&lt;? super T&gt;&gt; implements Serializable {
	/** Returns the raw types of the types in this set, in the same order. */
	public Set&lt;Class&lt;? super T&gt;&gt; rawTypes() {
	    // Java has no way to express ? super T when we parameterize TypeToken vs. Class.
	    @SuppressWarnings({ "unchecked", "rawtypes" })
	    ImmutableList&lt;Class&lt;? super T&gt;&gt; collectedTypes = (ImmutableList) TypeCollector.FOR_RAW_TYPE
		    .collectTypes(getRawTypes());
	    return ImmutableSet.copyOf(collectedTypes);
	}

    }

    private final Type runtimeType;

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

    /** Returns an instance of type token that wraps {@code type}. */
    public static TypeToken&lt;?&gt; of(Type type) {
	return new SimpleTypeToken&lt;&gt;(type);
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

    private TypeToken(Type type) {
	this.runtimeType = checkNotNull(type);
    }

    abstract class TypeCollector&lt;K&gt; {
	private final Type runtimeType;

	ImmutableList&lt;K&gt; collectTypes(Iterable&lt;? extends K&gt; types) {
	    // type -&gt; order number. 1 for Object, 2 for anything directly below, so on so forth.
	    Map&lt;K, Integer&gt; map = Maps.newHashMap();
	    for (K type : types) {
		collectTypes(type, map);
	    }
	    return sortKeysByValue(map, Ordering.natural().reverse());
	}

	/** Collects all types to map, and returns the total depth from T up to Object. */
	@CanIgnoreReturnValue
	private int collectTypes(K type, Map&lt;? super K, Integer&gt; map) {
	    Integer existing = map.get(type);
	    if (existing != null) {
		// short circuit: if set contains type it already contains its supertypes
		return existing;
	    }
	    // Interfaces should be listed before Object.
	    int aboveMe = getRawType(type).isInterface() ? 1 : 0;
	    for (K interfaceType : getInterfaces(type)) {
		aboveMe = Math.max(aboveMe, collectTypes(interfaceType, map));
	    }
	    K superclass = getSuperclass(type);
	    if (superclass != null) {
		aboveMe = Math.max(aboveMe, collectTypes(superclass, map));
	    }
	    /*
	     * TODO(benyu): should we include Object for interface? Also, CharSequence[] and Object[] for
	     * String[]?
	     *
	     */
	    map.put(type, aboveMe + 1);
	    return aboveMe + 1;
	}

	private static &lt;K, V&gt; ImmutableList&lt;K&gt; sortKeysByValue(final Map&lt;K, V&gt; map,
		final Comparator&lt;? super V&gt; valueComparator) {
	    Ordering&lt;K&gt; keyOrdering = new Ordering&lt;K&gt;() {
		@Override
		public int compare(K left, K right) {
		    return valueComparator.compare(map.get(left), map.get(right));
		}
	    };
	    return keyOrdering.immutableSortedCopy(map.keySet());
	}

	abstract Class&lt;?&gt; getRawType(K type);

	abstract Iterable&lt;? extends K&gt; getInterfaces(K type);

	abstract @Nullable K getSuperclass(K type);

    }

    class SimpleTypeToken&lt;T&gt; extends TypeToken&lt;T&gt; {
	private final Type runtimeType;

	SimpleTypeToken(Type type) {
	    super(type);
	}

    }

}

