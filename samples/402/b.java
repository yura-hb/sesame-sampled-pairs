import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permissions;
import java.security.ProtectionDomain;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanPermission;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import sun.reflect.misc.ReflectUtil;

class MBeanInstantiator {
    /**
     * Gets the class for the specified class name using the specified
     * class loader
     */
    public Class&lt;?&gt; findClass(String className, ObjectName aLoader)
	    throws ReflectionException, InstanceNotFoundException {

	if (aLoader == null)
	    throw new RuntimeOperationsException(new IllegalArgumentException(), "Null loader passed in parameter");

	// Retrieve the class loader from the repository
	ClassLoader loader = null;
	synchronized (this) {
	    loader = getClassLoader(aLoader);
	}
	if (loader == null) {
	    throw new InstanceNotFoundException(
		    "The loader named " + aLoader + " is not registered in the MBeanServer");
	}
	return findClass(className, loader);
    }

    private final ModifiableClassLoaderRepository clr;

    private ClassLoader getClassLoader(final ObjectName name) {
	if (clr == null) {
	    return null;
	}
	// Restrict to getClassLoader permission only
	Permissions permissions = new Permissions();
	permissions.add(new MBeanPermission("*", null, name, "getClassLoader"));
	ProtectionDomain protectionDomain = new ProtectionDomain(null, permissions);
	ProtectionDomain[] domains = { protectionDomain };
	AccessControlContext ctx = new AccessControlContext(domains);
	ClassLoader loader = AccessController.doPrivileged(new PrivilegedAction&lt;ClassLoader&gt;() {
	    public ClassLoader run() {
		return clr.getClassLoader(name);
	    }
	}, ctx);
	return loader;
    }

    /**
     * Gets the class for the specified class name using the MBean
     * Interceptor's classloader
     */
    public Class&lt;?&gt; findClass(String className, ClassLoader loader) throws ReflectionException {

	return loadClass(className, loader);
    }

    /**
     * Load a class with the specified loader, or with this object
     * class loader if the specified loader is null.
     **/
    static Class&lt;?&gt; loadClass(String className, ClassLoader loader) throws ReflectionException {
	Class&lt;?&gt; theClass;
	if (className == null) {
	    throw new RuntimeOperationsException(new IllegalArgumentException("The class name cannot be null"),
		    "Exception occurred during object instantiation");
	}
	ReflectUtil.checkPackageAccess(className);
	try {
	    if (loader == null)
		loader = MBeanInstantiator.class.getClassLoader();
	    if (loader != null) {
		theClass = Class.forName(className, false, loader);
	    } else {
		theClass = Class.forName(className);
	    }
	} catch (ClassNotFoundException e) {
	    throw new ReflectionException(e, "The MBean class could not be loaded");
	}
	return theClass;
    }

}

