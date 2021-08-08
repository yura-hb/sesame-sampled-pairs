import org.apache.commons.lang3.Validate;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class FieldUtils {
    /**
     * Removes the final modifier from a {@link Field}.
     *
     * @param field
     *            to remove the final modifier
     * @param forceAccess
     *            whether to break scope restrictions using the
     *            {@link java.lang.reflect.AccessibleObject#setAccessible(boolean)} method. {@code false} will only
     *            match {@code public} fields.
     * @throws IllegalArgumentException
     *             if the field is {@code null}
     * @since 3.3
     */
    public static void removeFinalModifier(final Field field, final boolean forceAccess) {
	Validate.isTrue(field != null, "The field must not be null");

	try {
	    if (Modifier.isFinal(field.getModifiers())) {
		// Do all JREs implement Field with a private ivar called "modifiers"?
		final Field modifiersField = Field.class.getDeclaredField("modifiers");
		final boolean doForceAccess = forceAccess && !modifiersField.isAccessible();
		if (doForceAccess) {
		    modifiersField.setAccessible(true);
		}
		try {
		    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		} finally {
		    if (doForceAccess) {
			modifiersField.setAccessible(false);
		    }
		}
	    }
	} catch (final NoSuchFieldException | IllegalAccessException ignored) {
	    // The field class contains always a modifiers field
	}
    }

}

