import com.google.common.collect.ImmutableList;

class Types {
    /** Returns a type where {@code rawType} is parameterized by {@code arguments}. */
    static ParameterizedType newParameterizedType(Class&lt;?&gt; rawType, Type... arguments) {
	return new ParameterizedTypeImpl(ClassOwnership.JVM_BEHAVIOR.getOwnerType(rawType), rawType, arguments);
    }

    private static void disallowPrimitiveType(Type[] types, String usedAs) {
	for (Type type : types) {
	    if (type instanceof Class) {
		Class&lt;?&gt; cls = (Class&lt;?&gt;) type;
		checkArgument(!cls.isPrimitive(), "Primitive type '%s' used as %s", cls, usedAs);
	    }
	}
    }

    class ClassOwnership extends Enum&lt;ClassOwnership&gt; {
	abstract @Nullable Class&lt;?&gt; getOwnerType(Class&lt;?&gt; rawType);

    }

    class ParameterizedTypeImpl implements ParameterizedType, Serializable {
	ParameterizedTypeImpl(@Nullable Type ownerType, Class&lt;?&gt; rawType, Type[] typeArguments) {
	    checkNotNull(rawType);
	    checkArgument(typeArguments.length == rawType.getTypeParameters().length);
	    disallowPrimitiveType(typeArguments, "type parameter");
	    this.ownerType = ownerType;
	    this.rawType = rawType;
	    this.argumentsList = JavaVersion.CURRENT.usedInGenericType(typeArguments);
	}

    }

    class JavaVersion extends Enum&lt;JavaVersion&gt; {
	final ImmutableList&lt;Type&gt; usedInGenericType(Type[] types) {
	    ImmutableList.Builder&lt;Type&gt; builder = ImmutableList.builder();
	    for (Type type : types) {
		builder.add(usedInGenericType(type));
	    }
	    return builder.build();
	}

	abstract Type usedInGenericType(Type type);

    }

}

