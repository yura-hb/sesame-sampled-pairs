import java.io.*;
import java.util.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

class Util {
    /**
    * Search the user hard-drive for a Java class library.
    * Returns null if none could be found.
    */
    public static String[] getJavaClassLibs() {
	String javaVersion = System.getProperty("java.version");
	int index = javaVersion.indexOf('.');
	if (index != -1) {
	    javaVersion = javaVersion.substring(0, index);
	} else {
	    index = javaVersion.indexOf('-');
	    if (index != -1)
		javaVersion = javaVersion.substring(0, index);
	}
	long jdkLevel = CompilerOptions.versionToJdkLevel(javaVersion);
	if (jdkLevel &gt;= ClassFileConstants.JDK9) {
	    String jreDir = getJREDirectory();
	    return new String[] { toNativePath(jreDir + "/lib/jrt-fs.jar") };
	}

	// check bootclasspath properties for Sun, JRockit and Harmony VMs
	String bootclasspathProperty = System.getProperty("sun.boot.class.path"); //$NON-NLS-1$
	if ((bootclasspathProperty == null) || (bootclasspathProperty.length() == 0)) {
	    // IBM J9 VMs
	    bootclasspathProperty = System.getProperty("vm.boot.class.path"); //$NON-NLS-1$
	    if ((bootclasspathProperty == null) || (bootclasspathProperty.length() == 0)) {
		// Harmony using IBM VME
		bootclasspathProperty = System.getProperty("org.apache.harmony.boot.class.path"); //$NON-NLS-1$
	    }
	}
	String[] jars = null;
	if ((bootclasspathProperty != null) && (bootclasspathProperty.length() != 0)) {
	    StringTokenizer tokenizer = new StringTokenizer(bootclasspathProperty, File.pathSeparator);
	    final int size = tokenizer.countTokens();
	    jars = new String[size];
	    int i = 0;
	    while (tokenizer.hasMoreTokens()) {
		final String fileName = toNativePath(tokenizer.nextToken());
		if (new File(fileName).exists()) {
		    jars[i] = fileName;
		    i++;
		}
	    }
	    if (size != i) {
		// resize
		System.arraycopy(jars, 0, (jars = new String[i]), 0, i);
	    }
	} else {
	    String jreDir = getJREDirectory();
	    final String osName = System.getProperty("os.name");
	    if (jreDir == null) {
		return new String[] {};
	    }
	    if (osName.startsWith("Mac")) {
		return new String[] { toNativePath(jreDir + "/../Classes/classes.jar") };
	    }
	    final String vmName = System.getProperty("java.vm.name");
	    if ("J9".equals(vmName)) {
		return new String[] { toNativePath(jreDir + "/lib/jclMax/classes.zip") };
	    }
	    String[] jarsNames = null;
	    ArrayList paths = new ArrayList();
	    if ("DRLVM".equals(vmName)) {
		FilenameFilter jarFilter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
			return name.endsWith(".jar") & !name.endsWith("-src.jar");
		    }
		};
		jarsNames = new File(jreDir + "/lib/boot/").list(jarFilter);
		addJarEntries(jreDir + "/lib/boot/", jarsNames, paths);
	    } else {
		jarsNames = new String[] { "/lib/vm.jar", "/lib/rt.jar", "/lib/core.jar", "/lib/security.jar",
			"/lib/xml.jar", "/lib/graphics.jar" };
		addJarEntries(jreDir, jarsNames, paths);
	    }
	    jars = new String[paths.size()];
	    paths.toArray(jars);
	}
	return jars;
    }

    /**
    * Returns the JRE directory this tests are running on.
    * Returns null if none could be found.
    *
    * Example of use: [org.eclipse.jdt.core.tests.util.Util.getJREDirectory()]
    */
    public static String getJREDirectory() {
	return System.getProperty("java.home");
    }

    /**
    * Makes the given path a path using native path separators as returned by File.getPath()
    * and trimming any extra slash.
    */
    public static String toNativePath(String path) {
	String nativePath = path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
	return nativePath.endsWith("/") || nativePath.endsWith("\\") ? nativePath.substring(0, nativePath.length() - 1)
		: nativePath;
    }

    private static void addJarEntries(String jreDir, String[] jarNames, ArrayList paths) {
	for (int i = 0, max = jarNames.length; i &lt; max; i++) {
	    final String currentName = jreDir + jarNames[i];
	    File f = new File(currentName);
	    if (f.exists()) {
		paths.add(toNativePath(currentName));
	    }
	}
    }

}

