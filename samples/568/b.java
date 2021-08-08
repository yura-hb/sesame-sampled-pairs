import static org.graalvm.compiler.core.common.UnsafeAccess.UNSAFE;
import org.graalvm.compiler.debug.GraalError;
import sun.misc.Unsafe;

class Fields {
    /**
     * Gets the value of a field for a given object.
     *
     * @param object the object whose field is to be read
     * @param index the index of the field (between 0 and {@link #getCount()})
     * @return the value of the specified field which will be boxed if the field type is primitive
     */
    public long getRawPrimitive(Object object, int index) {
	long offset = offsets[index];
	Class&lt;?&gt; type = types[index];

	if (type == Integer.TYPE) {
	    return UNSAFE.getInt(object, offset);
	} else if (type == Long.TYPE) {
	    return UNSAFE.getLong(object, offset);
	} else if (type == Boolean.TYPE) {
	    return UNSAFE.getBoolean(object, offset) ? 1 : 0;
	} else if (type == Float.TYPE) {
	    return Float.floatToRawIntBits(UNSAFE.getFloat(object, offset));
	} else if (type == Double.TYPE) {
	    return Double.doubleToRawLongBits(UNSAFE.getDouble(object, offset));
	} else if (type == Short.TYPE) {
	    return UNSAFE.getShort(object, offset);
	} else if (type == Character.TYPE) {
	    return UNSAFE.getChar(object, offset);
	} else if (type == Byte.TYPE) {
	    return UNSAFE.getByte(object, offset);
	} else {
	    throw GraalError.shouldNotReachHere();
	}
    }

    /**
     * Offsets used with {@link Unsafe} to access the fields.
     */
    protected final long[] offsets;
    /**
     * The types of the fields.
     */
    private final Class&lt;?&gt;[] types;

}

