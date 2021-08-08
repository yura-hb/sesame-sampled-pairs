import com.sun.org.apache.bcel.internal.classfile.JavaClass;

abstract class Repository {
    /**
     * @return true, if clazz is an implementation of interface inter
     * @throws ClassNotFoundException if clazz, inter, or any superclasses or
     * superinterfaces of clazz can't be found
     */
    public static boolean implementationOf(final String clazz, final String inter) throws ClassNotFoundException {
	return implementationOf(lookupClass(clazz), lookupClass(inter));
    }

    private static com.sun.org.apache.bcel.internal.util.Repository repository = SyntheticRepository.getInstance();

    /**
     * Lookup class somewhere found on your CLASSPATH, or whereever the
     * repository instance looks for it.
     *
     * @return class object for given fully qualified class name
     * @throws ClassNotFoundException if the class could not be found or parsed
     * correctly
     */
    public static JavaClass lookupClass(final String class_name) throws ClassNotFoundException {
	return repository.loadClass(class_name);
    }

    /**
     * @return true, if clazz is an implementation of interface inter
     * @throws ClassNotFoundException if any superclasses or superinterfaces of
     * clazz can't be found
     */
    public static boolean implementationOf(final JavaClass clazz, final JavaClass inter) throws ClassNotFoundException {
	return clazz.implementationOf(inter);
    }

}

