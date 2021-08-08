import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;

class Main implements Constants {
    /**
     * Get the correct type of BatchEnvironment
     */
    public BatchEnvironment getEnv() {

	ClassPath classPath = BatchEnvironment.createClassPath(classPathString, sysClassPathArg);
	BatchEnvironment result = null;
	try {
	    Class&lt;?&gt;[] ctorArgTypes = { OutputStream.class, ClassPath.class, Main.class };
	    Object[] ctorArgs = { out, classPath, this };
	    Constructor&lt;? extends BatchEnvironment&gt; constructor = environmentClass.getConstructor(ctorArgTypes);
	    result = constructor.newInstance(ctorArgs);
	    result.reset();
	} catch (Exception e) {
	    error("rmic.cannot.instantiate", environmentClass.getName());
	}
	return result;
    }

    String classPathString;
    String sysClassPathArg;
    /**
     * The stream where error message are printed.
     */
    OutputStream out;
    Class&lt;? extends BatchEnvironment&gt; environmentClass = BatchEnvironment.class;
    private static boolean resourcesInitialized = false;
    private static ResourceBundle resourcesExt = null;
    private static ResourceBundle resources;

    public void error(String msg, String arg1) {
	output(getText(msg, arg1));
    }

    public static String getText(String key, String arg0) {
	return getText(key, arg0, null, null);
    }

    /**
     * Output a message.
     */
    public void output(String msg) {
	PrintStream out = this.out instanceof PrintStream ? (PrintStream) this.out : new PrintStream(this.out, true);
	out.println(msg);
    }

    public static String getText(String key, String arg0, String arg1, String arg2) {
	String format = getString(key);
	if (format == null) {
	    format = "no text found: key = \"" + key + "\", " + "arguments = \"{0}\", \"{1}\", \"{2}\"";
	}

	String[] args = new String[3];
	args[0] = (arg0 != null ? arg0 : "null");
	args[1] = (arg1 != null ? arg1 : "null");
	args[2] = (arg2 != null ? arg2 : "null");

	return java.text.MessageFormat.format(format, (Object[]) args);
    }

    /**
     * Return the string value of a named resource in the rmic.properties
     * resource bundle.  If the resource is not found, null is returned.
     */
    public static String getString(String key) {
	if (!resourcesInitialized) {
	    initResources();
	}

	// To enable extensions, search the 'resourcesExt'
	// bundle first, followed by the 'resources' bundle...

	if (resourcesExt != null) {
	    try {
		return resourcesExt.getString(key);
	    } catch (MissingResourceException e) {
	    }
	}

	try {
	    return resources.getString(key);
	} catch (MissingResourceException ignore) {
	}
	return null;
    }

    private static void initResources() {
	try {
	    resources = ResourceBundle.getBundle("sun.rmi.rmic.resources.rmic");
	    resourcesInitialized = true;
	    try {
		resourcesExt = ResourceBundle.getBundle("sun.rmi.rmic.resources.rmicext");
	    } catch (MissingResourceException e) {
	    }
	} catch (MissingResourceException e) {
	    throw new Error("fatal: missing resource bundle: " + e.getClassName());
	}
    }

}

