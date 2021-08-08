import static jdk.nashorn.internal.runtime.ScriptRuntime.UNDEFINED;
import static jdk.nashorn.internal.runtime.UnwarrantedOptimismException.INVALID_PROGRAM_POINT;
import static jdk.nashorn.internal.runtime.UnwarrantedOptimismException.isValid;
import static jdk.nashorn.internal.runtime.arrays.ArrayIndex.isValidArrayIndex;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import jdk.nashorn.internal.runtime.arrays.ArrayData;
import jdk.nashorn.internal.runtime.arrays.ArrayIndex;

abstract class ScriptObject implements PropertyAccess, Cloneable {
    /**
     * Returns the set of {@literal &lt;property, value&gt;} entries that make up this
     * ScriptObject's properties
     * (java.util.Map-like method to help ScriptObjectMirror implementation)
     *
     * @return an entry set of all the properties in this object
     */
    public Set&lt;Map.Entry&lt;Object, Object&gt;&gt; entrySet() {
	final Iterator&lt;String&gt; iter = propertyIterator();
	final Set&lt;Map.Entry&lt;Object, Object&gt;&gt; entries = new HashSet&lt;&gt;();
	while (iter.hasNext()) {
	    final Object key = iter.next();
	    entries.add(new AbstractMap.SimpleImmutableEntry&lt;&gt;(key, get(key)));
	}
	return Collections.unmodifiableSet(entries);
    }

    /** Indexed array data. */
    private ArrayData arrayData;
    /** Map to property information and accessor functions. Ordered by insertion. */
    private PropertyMap map;
    /** objects proto. */
    private ScriptObject proto;
    /** Search fall back routine name for "no such property" */
    public static final String NO_SUCH_PROPERTY_NAME = "__noSuchProperty__";

    /**
     * Return a property iterator.
     * @return Property iterator.
     */
    public Iterator&lt;String&gt; propertyIterator() {
	return new KeyIterator(this);
    }

    @Override
    public Object get(final Object key) {
	final Object primitiveKey = JSType.toPrimitive(key, String.class);
	final int index = getArrayIndex(primitiveKey);
	final ArrayData array = getArray();

	if (array.has(index)) {
	    return array.getObject(index);
	}

	return get(index, JSType.toPropertyKey(primitiveKey));
    }

    /**
     * Get the {@link ArrayData} for this ScriptObject if it is an array
     * @return array data
     */
    public final ArrayData getArray() {
	return arrayData;
    }

    private Object get(final int index, final Object key) {
	if (isValidArrayIndex(index)) {
	    for (ScriptObject object = this;;) {
		if (object.getMap().containsArrayKeys()) {
		    final FindProperty find = object.findProperty(key, false);

		    if (find != null) {
			return find.getObjectValue();
		    }
		}

		if ((object = object.getProto()) == null) {
		    break;
		}

		final ArrayData array = object.getArray();

		if (array.has(index)) {
		    return array.getObject(index);
		}
	    }
	} else {
	    final FindProperty find = findProperty(key, true);

	    if (find != null) {
		return find.getObjectValue();
	    }
	}

	return invokeNoSuchProperty(key, false, INVALID_PROGRAM_POINT);
    }

    /**
     * Return the map of an object.
     * @return PropertyMap object.
     */
    public final PropertyMap getMap() {
	return map;
    }

    /**
     * Low level property API (not using property descriptors)
     * &lt;p&gt;
     * Find a property in the prototype hierarchy. Note: this is final and not
     * a good idea to override. If you have to, use
     * {jdk.nashorn.internal.objects.NativeArray{@link #getProperty(String)} or
     * {jdk.nashorn.internal.objects.NativeArray{@link #getPropertyDescriptor(String)} as the
     * overriding way to find array properties
     *
     * @see jdk.nashorn.internal.objects.NativeArray
     *
     * @param key  Property key.
     * @param deep Whether the search should look up proto chain.
     *
     * @return FindPropertyData or null if not found.
     */
    public final FindProperty findProperty(final Object key, final boolean deep) {
	return findProperty(key, deep, false, this);
    }

    /**
     * Return the __proto__ of an object.
     * @return __proto__ object.
     */
    public final ScriptObject getProto() {
	return proto;
    }

    /**
     * Invoke fall back if a property is not found.
     * @param key Name of property.
     * @param isScope is this a scope access?
     * @param programPoint program point
     * @return Result from call.
     */
    protected Object invokeNoSuchProperty(final Object key, final boolean isScope, final int programPoint) {
	final FindProperty find = findProperty(NO_SUCH_PROPERTY_NAME, true);
	final Object func = (find != null) ? find.getObjectValue() : null;

	Object ret = UNDEFINED;
	if (func instanceof ScriptFunction) {
	    final ScriptFunction sfunc = (ScriptFunction) func;
	    final Object self = isScope && sfunc.isStrict() ? UNDEFINED : this;
	    ret = ScriptRuntime.apply(sfunc, self, key);
	} else if (isScope) {
	    throw referenceError("not.defined", key.toString());
	}

	if (isValid(programPoint)) {
	    throw new UnwarrantedOptimismException(ret, programPoint);
	}

	return ret;
    }

    /**
     * Low level property API (not using property descriptors)
     * &lt;p&gt;
     * Find a property in the prototype hierarchy. Note: this is not a good idea
     * to override except as it was done in {@link WithObject}.
     * If you have to, use
     * {jdk.nashorn.internal.objects.NativeArray{@link #getProperty(String)} or
     * {jdk.nashorn.internal.objects.NativeArray{@link #getPropertyDescriptor(String)} as the
     * overriding way to find array properties
     *
     * @see jdk.nashorn.internal.objects.NativeArray
     *
     * @param key  Property key.
     * @param deep true if the search should look up proto chain
     * @param isScope true if this is a scope access
     * @param start the object on which the lookup was originally initiated
     * @return FindPropertyData or null if not found.
     */
    protected FindProperty findProperty(final Object key, final boolean deep, final boolean isScope,
	    final ScriptObject start) {

	final PropertyMap selfMap = getMap();
	final Property property = selfMap.findProperty(key);

	if (property != null) {
	    return new FindProperty(start, this, property);
	}

	if (deep) {
	    final ScriptObject myProto = getProto();
	    final FindProperty find = myProto == null ? null : myProto.findProperty(key, true, isScope, start);
	    // checkSharedProtoMap must be invoked after myProto.checkSharedProtoMap to propagate
	    // shared proto invalidation up the prototype chain. It also must be invoked when prototype is null.
	    checkSharedProtoMap();
	    return find;
	}

	return null;
    }

    private void checkSharedProtoMap() {
	// Check if our map has an expected shared prototype property map. If it has, make sure that
	// the prototype map has not been invalidated, and that it does match the actual map of the prototype.
	if (getMap().isInvalidSharedMapFor(getProto())) {
	    // Change our own map to one that does not assume a shared prototype map.
	    setMap(getMap().makeUnsharedCopy());
	}
    }

    /**
     * Set the initial map.
     * @param map Initial map.
     */
    public final void setMap(final PropertyMap map) {
	this.map = map;
    }

    class KeyIterator extends ScriptObjectIterator&lt;String&gt; {
	/** Indexed array data. */
	private ArrayData arrayData;
	/** Map to property information and accessor functions. Ordered by insertion. */
	private PropertyMap map;
	/** objects proto. */
	private ScriptObject proto;
	/** Search fall back routine name for "no such property" */
	public static final String NO_SUCH_PROPERTY_NAME = "__noSuchProperty__";

	KeyIterator(final ScriptObject object) {
	    super(object);
	}

    }

    abstract class ScriptObjectIterator&lt;T&gt; implements Iterator&lt;T&gt; {
	/** Indexed array data. */
	private ArrayData arrayData;
	/** Map to property information and accessor functions. Ordered by insertion. */
	private PropertyMap map;
	/** objects proto. */
	private ScriptObject proto;
	/** Search fall back routine name for "no such property" */
	public static final String NO_SUCH_PROPERTY_NAME = "__noSuchProperty__";

	ScriptObjectIterator(final ScriptObject object) {
	    this.object = object;
	}

    }

}

