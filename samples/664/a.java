import java.util.ArrayList;
import java.util.List;

class ClassUtils {
    /**
     * &lt;p&gt;Given a {@code List} of {@code Class} objects, this method converts
     * them into class names.&lt;/p&gt;
     *
     * &lt;p&gt;A new {@code List} is returned. {@code null} objects will be copied into
     * the returned list as {@code null}.&lt;/p&gt;
     *
     * @param classes  the classes to change
     * @return a {@code List} of class names corresponding to the Class objects,
     *  {@code null} if null input
     * @throws ClassCastException if {@code classes} contains a non-{@code Class} entry
     */
    public static List&lt;String&gt; convertClassesToClassNames(final List&lt;Class&lt;?&gt;&gt; classes) {
	if (classes == null) {
	    return null;
	}
	final List&lt;String&gt; classNames = new ArrayList&lt;&gt;(classes.size());
	for (final Class&lt;?&gt; cls : classes) {
	    if (cls == null) {
		classNames.add(null);
	    } else {
		classNames.add(cls.getName());
	    }
	}
	return classNames;
    }

}

