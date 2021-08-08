import java.lang.reflect.WildcardType;
import org.apache.commons.lang3.Validate;

class TypeUtils {
    /**
     * &lt;p&gt;Returns an array containing a single value of {@code null} if
     * {@link WildcardType#getLowerBounds()} returns an empty array. Otherwise,
     * it returns the result of {@link WildcardType#getLowerBounds()}.&lt;/p&gt;
     *
     * @param wildcardType the subject wildcard type, not {@code null}
     * @return a non-empty array containing the lower bounds of the wildcard
     * type.
     */
    public static Type[] getImplicitLowerBounds(final WildcardType wildcardType) {
	Validate.notNull(wildcardType, "wildcardType is null");
	final Type[] bounds = wildcardType.getLowerBounds();

	return bounds.length == 0 ? new Type[] { null } : bounds;
    }

}

