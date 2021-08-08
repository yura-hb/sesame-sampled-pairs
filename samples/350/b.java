import static jdk.nashorn.internal.runtime.ScriptRuntime.UNDEFINED;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.runtime.arrays.ArrayData;
import jdk.nashorn.internal.runtime.arrays.ArrayIndex;

abstract class ScriptObject implements PropertyAccess, Cloneable {
    /**
     * ECMA 8.12.1 [[GetOwnProperty]] (P)
     *
     * @param key property key
     *
     * @return Returns the Property Descriptor of the named own property of this
     * object, or undefined if absent.
     */
    public Object getOwnPropertyDescriptor(final Object key) {
	final Property property = getMap().findProperty(key);

	final Global global = Context.getGlobal();

	if (property != null) {
	    final ScriptFunction get = property.getGetterFunction(this);
	    final ScriptFunction set = property.getSetterFunction(this);

	    final boolean configurable = property.isConfigurable();
	    final boolean enumerable = property.isEnumerable();
	    final boolean writable = property.isWritable();

	    if (property.isAccessorProperty()) {
		return global.newAccessorDescriptor(get != null ? get : UNDEFINED, set != null ? set : UNDEFINED,
			configurable, enumerable);
	    }

	    return global.newDataDescriptor(getWithProperty(property), configurable, enumerable, writable);
	}

	final int index = getArrayIndex(key);
	final ArrayData array = getArray();

	if (array.has(index)) {
	    return array.getDescriptor(global, index);
	}

	return UNDEFINED;
    }

    /** Map to property information and accessor functions. Ordered by insertion. */
    private PropertyMap map;
    /** Indexed array data. */
    private ArrayData arrayData;

    /**
     * Return the map of an object.
     * @return PropertyMap object.
     */
    public final PropertyMap getMap() {
	return map;
    }

    /**
     * Get value using found property.
     *
     * @param property Found property.
     *
     * @return Value of property.
     */
    public final Object getWithProperty(final Property property) {
	return new FindProperty(this, this, property).getObjectValue();
    }

    /**
     * Get the {@link ArrayData} for this ScriptObject if it is an array
     * @return array data
     */
    public final ArrayData getArray() {
	return arrayData;
    }

}

