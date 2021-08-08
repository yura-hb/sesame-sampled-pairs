import java.util.Set;
import java.util.TreeSet;
import com.sun.org.apache.bcel.internal.util.ClassQueue;

class JavaClass extends AccessFlags implements Cloneable, Node, Comparable&lt;JavaClass&gt; {
    /**
     * Get all interfaces implemented by this JavaClass (transitively).
     */
    public JavaClass[] getAllInterfaces() throws ClassNotFoundException {
	final ClassQueue queue = new ClassQueue();
	final Set&lt;JavaClass&gt; allInterfaces = new TreeSet&lt;&gt;();
	queue.enqueue(this);
	while (!queue.empty()) {
	    final JavaClass clazz = queue.dequeue();
	    final JavaClass souper = clazz.getSuperClass();
	    final JavaClass[] _interfaces = clazz.getInterfaces();
	    if (clazz.isInterface()) {
		allInterfaces.add(clazz);
	    } else {
		if (souper != null) {
		    queue.enqueue(souper);
		}
	    }
	    for (final JavaClass _interface : _interfaces) {
		queue.enqueue(_interface);
	    }
	}
	return allInterfaces.toArray(new JavaClass[allInterfaces.size()]);
    }

    /**
     * In cases where we go ahead and create something, use the default
     * SyntheticRepository, because we don't know any better.
     */
    private transient com.sun.org.apache.bcel.internal.util.Repository repository = SyntheticRepository.getInstance();
    private String class_name;
    private String superclass_name;
    private String[] interface_names;

    /**
     * @return the superclass for this JavaClass object, or null if this is
     * java.lang.Object
     * @throws ClassNotFoundException if the superclass can't be found
     */
    public JavaClass getSuperClass() throws ClassNotFoundException {
	if ("java.lang.Object".equals(getClassName())) {
	    return null;
	}
	return repository.loadClass(getSuperclassName());
    }

    /**
     * Get interfaces directly implemented by this JavaClass.
     */
    public JavaClass[] getInterfaces() throws ClassNotFoundException {
	final String[] _interfaces = getInterfaceNames();
	final JavaClass[] classes = new JavaClass[_interfaces.length];
	for (int i = 0; i &lt; _interfaces.length; i++) {
	    classes[i] = repository.loadClass(_interfaces[i]);
	}
	return classes;
    }

    /**
     * @return Class name.
     */
    public String getClassName() {
	return class_name;
    }

    /**
     * returns the super class name of this class. In the case that this class
     * is java.lang.Object, it will return itself (java.lang.Object). This is
     * probably incorrect but isn't fixed at this time to not break existing
     * clients.
     *
     * @return Superclass name.
     */
    public String getSuperclassName() {
	return superclass_name;
    }

    /**
     * @return Names of implemented interfaces.
     */
    public String[] getInterfaceNames() {
	return interface_names;
    }

}

