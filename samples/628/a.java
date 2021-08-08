import java.io.File;
import java.util.*;

abstract class Nd4jBackend {
    /**
     * Adds the supplied Java Archive library to java.class.path. This is benign
     * if the library is already loaded.
     * @param jar the jar file to add
     * @throws NoAvailableBackendException
     */
    public static synchronized void loadLibrary(File jar) throws NoAvailableBackendException {
	try {
	    /*We are using reflection here to circumvent encapsulation; addURL is not public*/
	    java.net.URLClassLoader loader = (java.net.URLClassLoader) ClassLoader.getSystemClassLoader();
	    java.net.URL url = jar.toURI().toURL();
	    /*Disallow if already loaded*/
	    for (java.net.URL it : java.util.Arrays.asList(loader.getURLs())) {
		if (it.equals(url)) {
		    return;
		}
	    }
	    java.lang.reflect.Method method = java.net.URLClassLoader.class.getDeclaredMethod("addURL",
		    new Class[] { java.net.URL.class });
	    method.setAccessible(true); /*promote the method to public access*/
	    method.invoke(loader, new Object[] { url });
	} catch (final java.lang.NoSuchMethodException | java.lang.IllegalAccessException
		| java.net.MalformedURLException | java.lang.reflect.InvocationTargetException e) {
	    throw new NoAvailableBackendException(e);
	}
    }

    class NoAvailableBackendException extends Exception {
	/**
	 * Constructs a new exception with the specified cause and a detail
	 * message of &lt;tt&gt;(cause==null ? null : cause.toString())&lt;/tt&gt; (which
	 * typically contains the class and detail message of &lt;tt&gt;cause&lt;/tt&gt;).
	 * This constructor is useful for exceptions that are little more than
	 * wrappers for other throwables (for example, {@link
	 * PrivilegedActionException}).
	 *
	 * @param cause the cause (which is saved for later retrieval by the
	 *              {@link #getCause()} method).  (A &lt;tt&gt;null&lt;/tt&gt; value is
	 *              permitted, and indicates that the cause is nonexistent or
	 *              unknown.)
	 * @since 1.4
	 */
	public NoAvailableBackendException(Throwable cause) {
	    super(cause);
	}

    }

}

