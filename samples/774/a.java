import java.lang.reflect.Constructor;
import org.apache.commons.collections4.FunctorException;

class InstantiateFactory&lt;T&gt; implements Factory&lt;T&gt; {
    /**
     * Creates an object using the stored constructor.
     *
     * @return the new object
     */
    @Override
    public T create() {
	// needed for post-serialization
	if (iConstructor == null) {
	    findConstructor();
	}

	try {
	    return iConstructor.newInstance(iArgs);
	} catch (final InstantiationException ex) {
	    throw new FunctorException("InstantiateFactory: InstantiationException", ex);
	} catch (final IllegalAccessException ex) {
	    throw new FunctorException("InstantiateFactory: Constructor must be public", ex);
	} catch (final InvocationTargetException ex) {
	    throw new FunctorException("InstantiateFactory: Constructor threw an exception", ex);
	}
    }

    /** The constructor */
    private transient Constructor&lt;T&gt; iConstructor = null;
    /** The constructor arguments */
    private final Object[] iArgs;
    /** The class to create */
    private final Class&lt;T&gt; iClassToInstantiate;
    /** The constructor parameter types */
    private final Class&lt;?&gt;[] iParamTypes;

    /**
     * Find the Constructor for the class specified.
     */
    private void findConstructor() {
	try {
	    iConstructor = iClassToInstantiate.getConstructor(iParamTypes);
	} catch (final NoSuchMethodException ex) {
	    throw new IllegalArgumentException("InstantiateFactory: The constructor must exist and be public ");
	}
    }

}

