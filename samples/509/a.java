abstract class Invokable&lt;T, R&gt; extends Element implements GenericDeclaration {
    class ConstructorInvokable&lt;T&gt; extends Invokable&lt;T, T&gt; {
	/**
	* If the class is parameterized, such as {@link java.util.ArrayList ArrayList}, this returns
	* {@code ArrayList&lt;E&gt;}.
	*/
	@Override
	Type getGenericReturnType() {
	    Class&lt;?&gt; declaringClass = getDeclaringClass();
	    TypeVariable&lt;?&gt;[] typeParams = declaringClass.getTypeParameters();
	    if (typeParams.length &gt; 0) {
		return Types.newParameterizedType(declaringClass, typeParams);
	    } else {
		return declaringClass;
	    }
	}

    }

    @SuppressWarnings("unchecked") // The declaring class is T's raw class, or one of its supertypes.
    @Override
    public final Class&lt;? super T&gt; getDeclaringClass() {
	return (Class&lt;? super T&gt;) super.getDeclaringClass();
    }

}

