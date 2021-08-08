import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;

class TypeUtils {
    /**
     * Check equality of types.
     *
     * @param t1 the first type
     * @param t2 the second type
     * @return boolean
     * @since 3.2
     */
    public static boolean equals(final Type t1, final Type t2) {
	if (Objects.equals(t1, t2)) {
	    return true;
	}
	if (t1 instanceof ParameterizedType) {
	    return equals((ParameterizedType) t1, t2);
	}
	if (t1 instanceof GenericArrayType) {
	    return equals((GenericArrayType) t1, t2);
	}
	if (t1 instanceof WildcardType) {
	    return equals((WildcardType) t1, t2);
	}
	return false;
    }

    /**
     * Learn whether {@code t} equals {@code p}.
     * @param p LHS
     * @param t RHS
     * @return boolean
     * @since 3.2
     */
    private static boolean equals(final ParameterizedType p, final Type t) {
	if (t instanceof ParameterizedType) {
	    final ParameterizedType other = (ParameterizedType) t;
	    if (equals(p.getRawType(), other.getRawType()) && equals(p.getOwnerType(), other.getOwnerType())) {
		return equals(p.getActualTypeArguments(), other.getActualTypeArguments());
	    }
	}
	return false;
    }

    /**
     * Learn whether {@code t} equals {@code a}.
     * @param a LHS
     * @param t RHS
     * @return boolean
     * @since 3.2
     */
    private static boolean equals(final GenericArrayType a, final Type t) {
	return t instanceof GenericArrayType
		&& equals(a.getGenericComponentType(), ((GenericArrayType) t).getGenericComponentType());
    }

    /**
     * Learn whether {@code t} equals {@code w}.
     * @param w LHS
     * @param t RHS
     * @return boolean
     * @since 3.2
     */
    private static boolean equals(final WildcardType w, final Type t) {
	if (t instanceof WildcardType) {
	    final WildcardType other = (WildcardType) t;
	    return equals(getImplicitLowerBounds(w), getImplicitLowerBounds(other))
		    && equals(getImplicitUpperBounds(w), getImplicitUpperBounds(other));
	}
	return false;
    }

    /**
     * Learn whether {@code t1} equals {@code t2}.
     * @param t1 LHS
     * @param t2 RHS
     * @return boolean
     * @since 3.2
     */
    private static boolean equals(final Type[] t1, final Type[] t2) {
	if (t1.length == t2.length) {
	    for (int i = 0; i &lt; t1.length; i++) {
		if (!equals(t1[i], t2[i])) {
		    return false;
		}
	    }
	    return true;
	}
	return false;
    }

    /**
     * &lt;p&gt;Returns an array containing a single value of {@code null} if
     * {@link WildcardType#getLowerBounds()} returns an empty array. Otherwise,
     * it returns the result of {@link WildcardType#getLowerBounds()}.&lt;/p&gt;
     *
     * @param wildcardType the subject wildcard type, not {@code null}
     * @return a non-empty array containing the lower bounds of the wildcard
     * type.
     */
    public static Type[] getImplicitLowerBounds(final WildcardType wildcardType) {
	Validate.notNull(wildcardType, "wildcardType is null");
	final Type[] bounds = wildcardType.getLowerBounds();

	return bounds.length == 0 ? new Type[] { null } : bounds;
    }

    /**
     * &lt;p&gt;Returns an array containing the sole value of {@link Object} if
     * {@link WildcardType#getUpperBounds()} returns an empty array. Otherwise,
     * it returns the result of {@link WildcardType#getUpperBounds()}
     * passed into {@link #normalizeUpperBounds}.&lt;/p&gt;
     *
     * @param wildcardType the subject wildcard type, not {@code null}
     * @return a non-empty array containing the upper bounds of the wildcard
     * type.
     */
    public static Type[] getImplicitUpperBounds(final WildcardType wildcardType) {
	Validate.notNull(wildcardType, "wildcardType is null");
	final Type[] bounds = wildcardType.getUpperBounds();

	return bounds.length == 0 ? new Type[] { Object.class } : normalizeUpperBounds(bounds);
    }

    /**
     * &lt;p&gt;This method strips out the redundant upper bound types in type
     * variable types and wildcard types (or it would with wildcard types if
     * multiple upper bounds were allowed).&lt;/p&gt; &lt;p&gt;Example, with the variable
     * type declaration:
     *
     * &lt;pre&gt;&lt;K extends java.util.Collection&lt;String&gt; &amp;
     * java.util.List&lt;String&gt;&gt;&lt;/pre&gt;
     *
     * &lt;p&gt;
     * since {@code List} is a subinterface of {@code Collection},
     * this method will return the bounds as if the declaration had been:
     * &lt;/p&gt;
     *
     * &lt;pre&gt;&lt;K extends java.util.List&lt;String&gt;&gt;&lt;/pre&gt;
     *
     * @param bounds an array of types representing the upper bounds of either
     * {@link WildcardType} or {@link TypeVariable}, not {@code null}.
     * @return an array containing the values from {@code bounds} minus the
     * redundant types.
     */
    public static Type[] normalizeUpperBounds(final Type[] bounds) {
	Validate.notNull(bounds, "null value specified for bounds array");
	// don't bother if there's only one (or none) type
	if (bounds.length &lt; 2) {
	    return bounds;
	}

	final Set&lt;Type&gt; types = new HashSet&lt;&gt;(bounds.length);

	for (final Type type1 : bounds) {
	    boolean subtypeFound = false;

	    for (final Type type2 : bounds) {
		if (type1 != type2 && isAssignable(type2, type1, null)) {
		    subtypeFound = true;
		    break;
		}
	    }

	    if (!subtypeFound) {
		types.add(type1);
	    }
	}

	return types.toArray(new Type[types.size()]);
    }

    /**
     * &lt;p&gt;Checks if the subject type may be implicitly cast to the target type
     * following the Java generics rules.&lt;/p&gt;
     *
     * @param type the subject type to be assigned to the target type
     * @param toType the target type
     * @param typeVarAssigns optional map of type variable assignments
     * @return {@code true} if {@code type} is assignable to {@code toType}.
     */
    private static boolean isAssignable(final Type type, final Type toType,
	    final Map&lt;TypeVariable&lt;?&gt;, Type&gt; typeVarAssigns) {
	if (toType == null || toType instanceof Class&lt;?&gt;) {
	    return isAssignable(type, (Class&lt;?&gt;) toType);
	}

	if (toType instanceof ParameterizedType) {
	    return isAssignable(type, (ParameterizedType) toType, typeVarAssigns);
	}

	if (toType instanceof GenericArrayType) {
	    return isAssignable(type, (GenericArrayType) toType, typeVarAssigns);
	}

	if (toType instanceof WildcardType) {
	    return isAssignable(type, (WildcardType) toType, typeVarAssigns);
	}

	if (toType instanceof TypeVariable&lt;?&gt;) {
	    return isAssignable(type, (TypeVariable&lt;?&gt;) toType, typeVarAssigns);
	}

	throw new IllegalStateException("found an unhandled type: " + toType);
    }

    /**
     * &lt;p&gt;Checks if the subject type may be implicitly cast to the target class
     * following the Java generics rules.&lt;/p&gt;
     *
     * @param type the subject type to be assigned to the target type
     * @param toClass the target class
     * @return {@code true} if {@code type} is assignable to {@code toClass}.
     */
    private static boolean isAssignable(final Type type, final Class&lt;?&gt; toClass) {
	if (type == null) {
	    // consistency with ClassUtils.isAssignable() behavior
	    return toClass == null || !toClass.isPrimitive();
	}

	// only a null type can be assigned to null type which
	// would have cause the previous to return true
	if (toClass == null) {
	    return false;
	}

	// all types are assignable to themselves
	if (toClass.equals(type)) {
	    return true;
	}

	if (type instanceof Class&lt;?&gt;) {
	    // just comparing two classes
	    return ClassUtils.isAssignable((Class&lt;?&gt;) type, toClass);
	}

	if (type instanceof ParameterizedType) {
	    // only have to compare the raw type to the class
	    return isAssignable(getRawType((ParameterizedType) type), toClass);
	}

	// *
	if (type instanceof TypeVariable&lt;?&gt;) {
	    // if any of the bounds are assignable to the class, then the
	    // type is assignable to the class.
	    for (final Type bound : ((TypeVariable&lt;?&gt;) type).getBounds()) {
		if (isAssignable(bound, toClass)) {
		    return true;
		}
	    }

	    return false;
	}

	// the only classes to which a generic array type can be assigned
	// are class Object and array classes
	if (type instanceof GenericArrayType) {
	    return toClass.equals(Object.class) || toClass.isArray()
		    && isAssignable(((GenericArrayType) type).getGenericComponentType(), toClass.getComponentType());
	}

	// wildcard types are not assignable to a class (though one would think
	// "? super Object" would be assignable to Object)
	if (type instanceof WildcardType) {
	    return false;
	}

	throw new IllegalStateException("found an unhandled type: " + type);
    }

    /**
     * &lt;p&gt;Checks if the subject type may be implicitly cast to the target
     * parameterized type following the Java generics rules.&lt;/p&gt;
     *
     * @param type the subject type to be assigned to the target type
     * @param toParameterizedType the target parameterized type
     * @param typeVarAssigns a map with type variables
     * @return {@code true} if {@code type} is assignable to {@code toType}.
     */
    private static boolean isAssignable(final Type type, final ParameterizedType toParameterizedType,
	    final Map&lt;TypeVariable&lt;?&gt;, Type&gt; typeVarAssigns) {
	if (type == null) {
	    return true;
	}

	// only a null type can be assigned to null type which
	// would have cause the previous to return true
	if (toParameterizedType == null) {
	    return false;
	}

	// all types are assignable to themselves
	if (toParameterizedType.equals(type)) {
	    return true;
	}

	// get the target type's raw type
	final Class&lt;?&gt; toClass = getRawType(toParameterizedType);
	// get the subject type's type arguments including owner type arguments
	// and supertype arguments up to and including the target class.
	final Map&lt;TypeVariable&lt;?&gt;, Type&gt; fromTypeVarAssigns = getTypeArguments(type, toClass, null);

	// null means the two types are not compatible
	if (fromTypeVarAssigns == null) {
	    return false;
	}

	// compatible types, but there's no type arguments. this is equivalent
	// to comparing Map&lt; ?, ? &gt; to Map, and raw types are always assignable
	// to parameterized types.
	if (fromTypeVarAssigns.isEmpty()) {
	    return true;
	}

	// get the target type's type arguments including owner type arguments
	final Map&lt;TypeVariable&lt;?&gt;, Type&gt; toTypeVarAssigns = getTypeArguments(toParameterizedType, toClass,
		typeVarAssigns);

	// now to check each type argument
	for (final TypeVariable&lt;?&gt; var : toTypeVarAssigns.keySet()) {
	    final Type toTypeArg = unrollVariableAssignments(var, toTypeVarAssigns);
	    final Type fromTypeArg = unrollVariableAssignments(var, fromTypeVarAssigns);

	    if (toTypeArg == null && fromTypeArg instanceof Class) {
		continue;
	    }

	    // parameters must either be absent from the subject type, within
	    // the bounds of the wildcard type, or be an exact match to the
	    // parameters of the target type.
	    if (fromTypeArg != null && !toTypeArg.equals(fromTypeArg)
		    && !(toTypeArg instanceof WildcardType && isAssignable(fromTypeArg, toTypeArg, typeVarAssigns))) {
		return false;
	    }
	}
	return true;
    }

    /**
     * &lt;p&gt;Checks if the subject type may be implicitly cast to the target
     * generic array type following the Java generics rules.&lt;/p&gt;
     *
     * @param type the subject type to be assigned to the target type
     * @param toGenericArrayType the target generic array type
     * @param typeVarAssigns a map with type variables
     * @return {@code true} if {@code type} is assignable to
     * {@code toGenericArrayType}.
     */
    private static boolean isAssignable(final Type type, final GenericArrayType toGenericArrayType,
	    final Map&lt;TypeVariable&lt;?&gt;, Type&gt; typeVarAssigns) {
	if (type == null) {
	    return true;
	}

	// only a null type can be assigned to null type which
	// would have cause the previous to return true
	if (toGenericArrayType == null) {
	    return false;
	}

	// all types are assignable to themselves
	if (toGenericArrayType.equals(type)) {
	    return true;
	}

	final Type toComponentType = toGenericArrayType.getGenericComponentType();

	if (type instanceof Class&lt;?&gt;) {
	    final Class&lt;?&gt; cls = (Class&lt;?&gt;) type;

	    // compare the component types
	    return cls.isArray() && isAssignable(cls.getComponentType(), toComponentType, typeVarAssigns);
	}

	if (type instanceof GenericArrayType) {
	    // compare the component types
	    return isAssignable(((GenericArrayType) type).getGenericComponentType(), toComponentType, typeVarAssigns);
	}

	if (type instanceof WildcardType) {
	    // so long as one of the upper bounds is assignable, it's good
	    for (final Type bound : getImplicitUpperBounds((WildcardType) type)) {
		if (isAssignable(bound, toGenericArrayType)) {
		    return true;
		}
	    }

	    return false;
	}

	if (type instanceof TypeVariable&lt;?&gt;) {
	    // probably should remove the following logic and just return false.
	    // type variables cannot specify arrays as bounds.
	    for (final Type bound : getImplicitBounds((TypeVariable&lt;?&gt;) type)) {
		if (isAssignable(bound, toGenericArrayType)) {
		    return true;
		}
	    }

	    return false;
	}

	if (type instanceof ParameterizedType) {
	    // the raw type of a parameterized type is never an array or
	    // generic array, otherwise the declaration would look like this:
	    // Collection[]&lt; ? extends String &gt; collection;
	    return false;
	}

	throw new IllegalStateException("found an unhandled type: " + type);
    }

    /**
     * &lt;p&gt;Checks if the subject type may be implicitly cast to the target
     * wildcard type following the Java generics rules.&lt;/p&gt;
     *
     * @param type the subject type to be assigned to the target type
     * @param toWildcardType the target wildcard type
     * @param typeVarAssigns a map with type variables
     * @return {@code true} if {@code type} is assignable to
     * {@code toWildcardType}.
     */
    private static boolean isAssignable(final Type type, final WildcardType toWildcardType,
	    final Map&lt;TypeVariable&lt;?&gt;, Type&gt; typeVarAssigns) {
	if (type == null) {
	    return true;
	}

	// only a null type can be assigned to null type which
	// would have cause the previous to return true
	if (toWildcardType == null) {
	    return false;
	}

	// all types are assignable to themselves
	if (toWildcardType.equals(type)) {
	    return true;
	}

	final Type[] toUpperBounds = getImplicitUpperBounds(toWildcardType);
	final Type[] toLowerBounds = getImplicitLowerBounds(toWildcardType);

	if (type instanceof WildcardType) {
	    final WildcardType wildcardType = (WildcardType) type;
	    final Type[] upperBounds = getImplicitUpperBounds(wildcardType);
	    final Type[] lowerBounds = getImplicitLowerBounds(wildcardType);

	    for (Type toBound : toUpperBounds) {
		// if there are assignments for unresolved type variables,
		// now's the time to substitute them.
		toBound = substituteTypeVariables(toBound, typeVarAssigns);

		// each upper bound of the subject type has to be assignable to
		// each
		// upper bound of the target type
		for (final Type bound : upperBounds) {
		    if (!isAssignable(bound, toBound, typeVarAssigns)) {
			return false;
		    }
		}
	    }

	    for (Type toBound : toLowerBounds) {
		// if there are assignments for unresolved type variables,
		// now's the time to substitute them.
		toBound = substituteTypeVariables(toBound, typeVarAssigns);

		// each lower bound of the target type has to be assignable to
		// each
		// lower bound of the subject type
		for (final Type bound : lowerBounds) {
		    if (!isAssignable(toBound, bound, typeVarAssigns)) {
			return false;
		    }
		}
	    }
	    return true;
	}

	for (final Type toBound : toUpperBounds) {
	    // if there are assignments for unresolved type variables,
	    // now's the time to substitute them.
	    if (!isAssignable(type, substituteTypeVariables(toBound, typeVarAssigns), typeVarAssigns)) {
		return false;
	    }
	}

	for (final Type toBound : toLowerBounds) {
	    // if there are assignments for unresolved type variables,
	    // now's the time to substitute them.
	    if (!isAssignable(substituteTypeVariables(toBound, typeVarAssigns), type, typeVarAssigns)) {
		return false;
	    }
	}
	return true;
    }

    /**
     * &lt;p&gt;Checks if the subject type may be implicitly cast to the target type
     * variable following the Java generics rules.&lt;/p&gt;
     *
     * @param type the subject type to be assigned to the target type
     * @param toTypeVariable the target type variable
     * @param typeVarAssigns a map with type variables
     * @return {@code true} if {@code type} is assignable to
     * {@code toTypeVariable}.
     */
    private static boolean isAssignable(final Type type, final TypeVariable&lt;?&gt; toTypeVariable,
	    final Map&lt;TypeVariable&lt;?&gt;, Type&gt; typeVarAssigns) {
	if (type == null) {
	    return true;
	}

	// only a null type can be assigned to null type which
	// would have cause the previous to return true
	if (toTypeVariable == null) {
	    return false;
	}

	// all types are assignable to themselves
	if (toTypeVariable.equals(type)) {
	    return true;
	}

	if (type instanceof TypeVariable&lt;?&gt;) {
	    // a type variable is assignable to another type variable, if
	    // and only if the former is the latter, extends the latter, or
	    // is otherwise a descendant of the latter.
	    final Type[] bounds = getImplicitBounds((TypeVariable&lt;?&gt;) type);

	    for (final Type bound : bounds) {
		if (isAssignable(bound, toTypeVariable, typeVarAssigns)) {
		    return true;
		}
	    }
	}

	if (type instanceof Class&lt;?&gt; || type instanceof ParameterizedType || type instanceof GenericArrayType
		|| type instanceof WildcardType) {
	    return false;
	}

	throw new IllegalStateException("found an unhandled type: " + type);
    }

    /**
     * &lt;p&gt;Transforms the passed in type to a {@link Class} object. Type-checking method of convenience.&lt;/p&gt;
     *
     * @param parameterizedType the type to be converted
     * @return the corresponding {@code Class} object
     * @throws IllegalStateException if the conversion fails
     */
    private static Class&lt;?&gt; getRawType(final ParameterizedType parameterizedType) {
	final Type rawType = parameterizedType.getRawType();

	// check if raw type is a Class object
	// not currently necessary, but since the return type is Type instead of
	// Class, there's enough reason to believe that future versions of Java
	// may return other Type implementations. And type-safety checking is
	// rarely a bad idea.
	if (!(rawType instanceof Class&lt;?&gt;)) {
	    throw new IllegalStateException("Wait... What!? Type of rawType: " + rawType);
	}

	return (Class&lt;?&gt;) rawType;
    }

    /**
     * &lt;p&gt;Return a map of the type arguments of {@code type} in the context of {@code toClass}.&lt;/p&gt;
     *
     * @param type the type in question
     * @param toClass the class
     * @param subtypeVarAssigns a map with type variables
     * @return the {@code Map} with type arguments
     */
    private static Map&lt;TypeVariable&lt;?&gt;, Type&gt; getTypeArguments(final Type type, final Class&lt;?&gt; toClass,
	    final Map&lt;TypeVariable&lt;?&gt;, Type&gt; subtypeVarAssigns) {
	if (type instanceof Class&lt;?&gt;) {
	    return getTypeArguments((Class&lt;?&gt;) type, toClass, subtypeVarAssigns);
	}

	if (type instanceof ParameterizedType) {
	    return getTypeArguments((ParameterizedType) type, toClass, subtypeVarAssigns);
	}

	if (type instanceof GenericArrayType) {
	    return getTypeArguments(((GenericArrayType) type).getGenericComponentType(),
		    toClass.isArray() ? toClass.getComponentType() : toClass, subtypeVarAssigns);
	}

	// since wildcard types are not assignable to classes, should this just
	// return null?
	if (type instanceof WildcardType) {
	    for (final Type bound : getImplicitUpperBounds((WildcardType) type)) {
		// find the first bound that is assignable to the target class
		if (isAssignable(bound, toClass)) {
		    return getTypeArguments(bound, toClass, subtypeVarAssigns);
		}
	    }

	    return null;
	}

	if (type instanceof TypeVariable&lt;?&gt;) {
	    for (final Type bound : getImplicitBounds((TypeVariable&lt;?&gt;) type)) {
		// find the first bound that is assignable to the target class
		if (isAssignable(bound, toClass)) {
		    return getTypeArguments(bound, toClass, subtypeVarAssigns);
		}
	    }

	    return null;
	}
	throw new IllegalStateException("found an unhandled type: " + type);
    }

    /**
     * &lt;p&gt;Return a map of the type arguments of a parameterized type in the context of {@code toClass}.&lt;/p&gt;
     *
     * @param parameterizedType the parameterized type
     * @param toClass the class
     * @param subtypeVarAssigns a map with type variables
     * @return the {@code Map} with type arguments
     */
    private static Map&lt;TypeVariable&lt;?&gt;, Type&gt; getTypeArguments(final ParameterizedType parameterizedType,
	    final Class&lt;?&gt; toClass, final Map&lt;TypeVariable&lt;?&gt;, Type&gt; subtypeVarAssigns) {
	final Class&lt;?&gt; cls = getRawType(parameterizedType);

	// make sure they're assignable
	if (!isAssignable(cls, toClass)) {
	    return null;
	}

	final Type ownerType = parameterizedType.getOwnerType();
	Map&lt;TypeVariable&lt;?&gt;, Type&gt; typeVarAssigns;

	if (ownerType instanceof ParameterizedType) {
	    // get the owner type arguments first
	    final ParameterizedType parameterizedOwnerType = (ParameterizedType) ownerType;
	    typeVarAssigns = getTypeArguments(parameterizedOwnerType, getRawType(parameterizedOwnerType),
		    subtypeVarAssigns);
	} else {
	    // no owner, prep the type variable assignments map
	    typeVarAssigns = subtypeVarAssigns == null ? new HashMap&lt;&gt;() : new HashMap&lt;&gt;(subtypeVarAssigns);
	}

	// get the subject parameterized type's arguments
	final Type[] typeArgs = parameterizedType.getActualTypeArguments();
	// and get the corresponding type variables from the raw class
	final TypeVariable&lt;?&gt;[] typeParams = cls.getTypeParameters();

	// map the arguments to their respective type variables
	for (int i = 0; i &lt; typeParams.length; i++) {
	    final Type typeArg = typeArgs[i];
	    typeVarAssigns.put(typeParams[i],
		    typeVarAssigns.containsKey(typeArg) ? typeVarAssigns.get(typeArg) : typeArg);
	}

	if (toClass.equals(cls)) {
	    // target class has been reached. Done.
	    return typeVarAssigns;
	}

	// walk the inheritance hierarchy until the target class is reached
	return getTypeArguments(getClosestParentType(cls, toClass), toClass, typeVarAssigns);
    }

    /**
     * Look up {@code var} in {@code typeVarAssigns} &lt;em&gt;transitively&lt;/em&gt;,
     * i.e. keep looking until the value found is &lt;em&gt;not&lt;/em&gt; a type variable.
     * @param var the type variable to look up
     * @param typeVarAssigns the map used for the look up
     * @return Type or {@code null} if some variable was not in the map
     * @since 3.2
     */
    private static Type unrollVariableAssignments(TypeVariable&lt;?&gt; var,
	    final Map&lt;TypeVariable&lt;?&gt;, Type&gt; typeVarAssigns) {
	Type result;
	do {
	    result = typeVarAssigns.get(var);
	    if (result instanceof TypeVariable&lt;?&gt; && !result.equals(var)) {
		var = (TypeVariable&lt;?&gt;) result;
		continue;
	    }
	    break;
	} while (true);
	return result;
    }

    /**
     * &lt;p&gt;Checks if the subject type may be implicitly cast to the target type
     * following the Java generics rules. If both types are {@link Class}
     * objects, the method returns the result of
     * {@link ClassUtils#isAssignable(Class, Class)}.&lt;/p&gt;
     *
     * @param type the subject type to be assigned to the target type
     * @param toType the target type
     * @return {@code true} if {@code type} is assignable to {@code toType}.
     */
    public static boolean isAssignable(final Type type, final Type toType) {
	return isAssignable(type, toType, null);
    }

    /**
     * &lt;p&gt;Returns an array containing the sole type of {@link Object} if
     * {@link TypeVariable#getBounds()} returns an empty array. Otherwise, it
     * returns the result of {@link TypeVariable#getBounds()} passed into
     * {@link #normalizeUpperBounds}.&lt;/p&gt;
     *
     * @param typeVariable the subject type variable, not {@code null}
     * @return a non-empty array containing the bounds of the type variable.
     */
    public static Type[] getImplicitBounds(final TypeVariable&lt;?&gt; typeVariable) {
	Validate.notNull(typeVariable, "typeVariable is null");
	final Type[] bounds = typeVariable.getBounds();

	return bounds.length == 0 ? new Type[] { Object.class } : normalizeUpperBounds(bounds);
    }

    /**
     * &lt;p&gt;Find the mapping for {@code type} in {@code typeVarAssigns}.&lt;/p&gt;
     *
     * @param type the type to be replaced
     * @param typeVarAssigns the map with type variables
     * @return the replaced type
     * @throws IllegalArgumentException if the type cannot be substituted
     */
    private static Type substituteTypeVariables(final Type type, final Map&lt;TypeVariable&lt;?&gt;, Type&gt; typeVarAssigns) {
	if (type instanceof TypeVariable&lt;?&gt; && typeVarAssigns != null) {
	    final Type replacementType = typeVarAssigns.get(type);

	    if (replacementType == null) {
		throw new IllegalArgumentException("missing assignment type for type variable " + type);
	    }
	    return replacementType;
	}
	return type;
    }

    /**
     * &lt;p&gt;Return a map of the type arguments of a class in the context of {@code toClass}.&lt;/p&gt;
     *
     * @param cls the class in question
     * @param toClass the context class
     * @param subtypeVarAssigns a map with type variables
     * @return the {@code Map} with type arguments
     */
    private static Map&lt;TypeVariable&lt;?&gt;, Type&gt; getTypeArguments(Class&lt;?&gt; cls, final Class&lt;?&gt; toClass,
	    final Map&lt;TypeVariable&lt;?&gt;, Type&gt; subtypeVarAssigns) {
	// make sure they're assignable
	if (!isAssignable(cls, toClass)) {
	    return null;
	}

	// can't work with primitives
	if (cls.isPrimitive()) {
	    // both classes are primitives?
	    if (toClass.isPrimitive()) {
		// dealing with widening here. No type arguments to be
		// harvested with these two types.
		return new HashMap&lt;&gt;();
	    }

	    // work with wrapper the wrapper class instead of the primitive
	    cls = ClassUtils.primitiveToWrapper(cls);
	}

	// create a copy of the incoming map, or an empty one if it's null
	final HashMap&lt;TypeVariable&lt;?&gt;, Type&gt; typeVarAssigns = subtypeVarAssigns == null ? new HashMap&lt;&gt;()
		: new HashMap&lt;&gt;(subtypeVarAssigns);

	// has target class been reached?
	if (toClass.equals(cls)) {
	    return typeVarAssigns;
	}

	// walk the inheritance hierarchy until the target class is reached
	return getTypeArguments(getClosestParentType(cls, toClass), toClass, typeVarAssigns);
    }

    /**
     * &lt;p&gt;Get the closest parent type to the
     * super class specified by {@code superClass}.&lt;/p&gt;
     *
     * @param cls the class in question
     * @param superClass the super class
     * @return the closes parent type
     */
    private static Type getClosestParentType(final Class&lt;?&gt; cls, final Class&lt;?&gt; superClass) {
	// only look at the interfaces if the super class is also an interface
	if (superClass.isInterface()) {
	    // get the generic interfaces of the subject class
	    final Type[] interfaceTypes = cls.getGenericInterfaces();
	    // will hold the best generic interface match found
	    Type genericInterface = null;

	    // find the interface closest to the super class
	    for (final Type midType : interfaceTypes) {
		Class&lt;?&gt; midClass = null;

		if (midType instanceof ParameterizedType) {
		    midClass = getRawType((ParameterizedType) midType);
		} else if (midType instanceof Class&lt;?&gt;) {
		    midClass = (Class&lt;?&gt;) midType;
		} else {
		    throw new IllegalStateException("Unexpected generic" + " interface type found: " + midType);
		}

		// check if this interface is further up the inheritance chain
		// than the previously found match
		if (isAssignable(midClass, superClass) && isAssignable(genericInterface, (Type) midClass)) {
		    genericInterface = midType;
		}
	    }

	    // found a match?
	    if (genericInterface != null) {
		return genericInterface;
	    }
	}

	// none of the interfaces were descendants of the target class, so the
	// super class has to be one, instead
	return cls.getGenericSuperclass();
    }

}

