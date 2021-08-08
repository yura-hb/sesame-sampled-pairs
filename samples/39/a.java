import java.lang.reflect.Field;

class TokenUtil {
    /**
     * Gets the value of a static or instance field of type int or of another primitive type
     * convertible to type int via a widening conversion. Does not throw any checked exceptions.
     * @param field from which the int should be extracted
     * @param object to extract the int value from
     * @return the value of the field converted to type int
     * @throws IllegalStateException if this Field object is enforcing Java language access control
     *         and the underlying field is inaccessible
     * @see Field#getInt(Object)
     */
    public static int getIntFromField(Field field, Object object) {
	try {
	    return field.getInt(object);
	} catch (final IllegalAccessException exception) {
	    throw new IllegalStateException(exception);
	}
    }

}

