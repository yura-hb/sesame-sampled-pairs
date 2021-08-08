import com.google.common.collect.ImmutableList;
import java.lang.reflect.WildcardType;

class Types {
    /** Returns the array type of {@code componentType}. */
    static Type newArrayType(Type componentType) {
	if (componentType instanceof WildcardType) {
	    WildcardType wildcard = (WildcardType) componentType;
	    Type[] lowerBounds = wildcard.getLowerBounds();
	    checkArgument(lowerBounds.length &lt;= 1, "Wildcard cannot have more than one lower bounds.");
	    if (lowerBounds.length == 1) {
		return supertypeOf(newArrayType(lowerBounds[0]));
	    } else {
		Type[] upperBounds = wildcard.getUpperBounds();
		checkArgument(upperBounds.length == 1, "Wildcard should have only one upper bound.");
		return subtypeOf(newArrayType(upperBounds[0]));
	    }
	}
	return JavaVersion.CURRENT.newArrayType(componentType);
    }

    /** Returns a new {@link WildcardType} with {@code lowerBound}. */
    @VisibleForTesting
    static WildcardType supertypeOf(Type lowerBound) {
	return new WildcardTypeImpl(new Type[] { lowerBound }, new Type[] { Object.class });
    }

    /** Returns a new {@link WildcardType} with {@code upperBound}. */
    @VisibleForTesting
    static WildcardType subtypeOf(Type upperBound) {
	return new WildcardTypeImpl(new Type[0], new Type[] { upperBound });
    }

    private static void disallowPrimitiveType(Type[] types, String usedAs) {
	for (Type type : types) {
	    if (type instanceof Class) {
		Class&lt;?&gt; cls = (Class&lt;?&gt;) type;
		checkArgument(!cls.isPrimitive(), "Primitive type '%s' used as %s", cls, usedAs);
	    }
	}
    }

    class JavaVersion extends Enum&lt;JavaVersion&gt; {
	abstract Type newArrayType(Type componentType);

	final ImmutableList&lt;Type&gt; usedInGenericType(Type[] types) {
	    ImmutableList.Builder&lt;Type&gt; builder = ImmutableList.builder();
	    for (Type type : types) {
		builder.add(usedInGenericType(type));
	    }
	    return builder.build();
	}

	abstract Type usedInGenericType(Type type);

    }

    class WildcardTypeImpl implements WildcardType, Serializable {
	WildcardTypeImpl(Type[] lowerBounds, Type[] upperBounds) {
	    disallowPrimitiveType(lowerBounds, "lower bound for wildcard");
	    disallowPrimitiveType(upperBounds, "upper bound for wildcard");
	    this.lowerBounds = JavaVersion.CURRENT.usedInGenericType(lowerBounds);
	    this.upperBounds = JavaVersion.CURRENT.usedInGenericType(upperBounds);
	}

    }

}

