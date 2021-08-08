import java.lang.reflect.Array;

class ObjectArrays {
    /**
    * Returns a new array that contains the concatenated contents of two arrays.
    *
    * @param first the first array of elements to concatenate
    * @param second the second array of elements to concatenate
    * @param type the component type of the returned array
    */
    @GwtIncompatible // Array.newInstance(Class, int)
    public static &lt;T&gt; T[] concat(T[] first, T[] second, Class&lt;T&gt; type) {
	T[] result = newArray(type, first.length + second.length);
	System.arraycopy(first, 0, result, 0, first.length);
	System.arraycopy(second, 0, result, first.length, second.length);
	return result;
    }

    /**
    * Returns a new array of the given length with the specified component type.
    *
    * @param type the component type
    * @param length the length of the new array
    */
    @GwtIncompatible // Array.newInstance(Class, int)
    @SuppressWarnings("unchecked")
    public static &lt;T&gt; T[] newArray(Class&lt;T&gt; type, int length) {
	return (T[]) Array.newInstance(type, length);
    }

}

