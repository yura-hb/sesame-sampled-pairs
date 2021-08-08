import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.FullIdent;

abstract class AbstractTypeAwareCheck extends AbstractCheck {
    /**
     * Tries to load class. Logs error if unable.
     * @param ident name of class which we try to load.
     * @param className name of surrounding class.
     * @return {@code Class} for a ident.
     * @noinspection WeakerAccess
     */
    // -@cs[ForbidWildcardAsReturnType] The class is deprecated and will be removed soon.
    protected final Class&lt;?&gt; tryLoadClass(Token ident, String className) {
	final Class&lt;?&gt; clazz = resolveClass(ident.getText(), className);
	if (clazz == null) {
	    logLoadError(ident);
	}
	return clazz;
    }

    /** {@code ClassResolver} instance for current tree. */
    private ClassResolver classResolver;
    /** Full identifier for package of the method. **/
    private FullIdent packageFullIdent;
    /** Imports details. **/
    private final Set&lt;String&gt; imports = new HashSet&lt;&gt;();

    /**
     * Attempts to resolve the Class for a specified name.
     * @param resolvableClassName name of the class to resolve
     * @param className name of surrounding class.
     * @return the resolved class or {@code null}
     *          if unable to resolve the class.
     * @noinspection WeakerAccess
     */
    // -@cs[ForbidWildcardAsReturnType] The class is deprecated and will be removed soon.
    protected final Class&lt;?&gt; resolveClass(String resolvableClassName, String className) {
	Class&lt;?&gt; clazz;
	try {
	    clazz = getClassResolver().resolve(resolvableClassName, className);
	} catch (final ClassNotFoundException ignored) {
	    clazz = null;
	}
	return clazz;
    }

    /**
     * Logs error if unable to load class information.
     * Abstract, should be overridden in subclasses.
     * @param ident class name for which we can no load class.
     */
    protected abstract void logLoadError(Token ident);

    /**
     * Returns the current tree's ClassResolver.
     * @return {@code ClassResolver} for current tree.
     */
    private ClassResolver getClassResolver() {
	if (classResolver == null) {
	    classResolver = new ClassResolver(getClassLoader(), packageFullIdent.getText(), imports);
	}
	return classResolver;
    }

    class Token {
	/** {@code ClassResolver} instance for current tree. */
	private ClassResolver classResolver;
	/** Full identifier for package of the method. **/
	private FullIdent packageFullIdent;
	/** Imports details. **/
	private final Set&lt;String&gt; imports = new HashSet&lt;&gt;();

	/**
	 * Gets text of the token.
	 * @return text of the token
	 */
	public String getText() {
	    return text;
	}

    }

}

