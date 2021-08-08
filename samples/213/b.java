import java.io.*;
import java.util.*;
import javax.tools.*;
import com.sun.tools.classfile.*;

class CheckResourceKeys {
    /**
     * Main entry point.
     */
    boolean run(String... args) throws Exception {
	boolean findDeadKeys = false;
	boolean findMissingKeys = false;

	if (args.length == 0) {
	    if (is_jtreg()) {
		findDeadKeys = true;
		findMissingKeys = true;
	    } else {
		System.err.println("Usage: java CheckResourceKeys &lt;options&gt;");
		System.err.println("where options include");
		System.err.println("  -finddeadkeys      find keys in resource bundles which are no longer required");
		System.err.println("  -findmissingkeys   find keys in resource bundles that are required but missing");
		return true;
	    }
	} else {
	    for (String arg : args) {
		if (arg.equalsIgnoreCase("-finddeadkeys"))
		    findDeadKeys = true;
		else if (arg.equalsIgnoreCase("-findmissingkeys"))
		    findMissingKeys = true;
		else
		    error("bad option: " + arg);
	    }
	}

	if (errors &gt; 0)
	    return false;

	Set&lt;String&gt; codeKeys = getCodeKeys();
	Set&lt;String&gt; resourceKeys = getResourceKeys();

	System.err.println("found " + codeKeys.size() + " keys in code");
	System.err.println("found " + resourceKeys.size() + " keys in resource bundles");

	if (findDeadKeys)
	    findDeadKeys(codeKeys, resourceKeys);

	if (findMissingKeys)
	    findMissingKeys(codeKeys, resourceKeys);

	return (errors == 0);
    }

    int errors;

    static boolean is_jtreg() {
	return (System.getProperty("test.src") != null);
    }

    /**
     * Report an error.
     */
    void error(String msg) {
	System.err.println("Error: " + msg);
	errors++;
    }

    /**
     * Get the set of strings from (most of) the javadoc classfiles.
     */
    Set&lt;String&gt; getCodeKeys() throws IOException {
	Set&lt;String&gt; results = new TreeSet&lt;String&gt;();
	JavaCompiler c = ToolProvider.getSystemJavaCompiler();
	try (JavaFileManager fm = c.getStandardFileManager(null, null, null)) {
	    JavaFileManager.Location javadocLoc = findJavadocLocation(fm);
	    String[] pkgs = { "com.sun.tools.javadoc" };
	    for (String pkg : pkgs) {
		for (JavaFileObject fo : fm.list(javadocLoc, pkg, EnumSet.of(JavaFileObject.Kind.CLASS), true)) {
		    String name = fo.getName();
		    // ignore resource files
		    if (name.matches(".*resources.[A-Za-z_0-9]+\\.class.*"))
			continue;
		    scan(fo, results);
		}
	    }

	    // special handling for code strings synthesized in
	    // com.sun.tools.doclets.internal.toolkit.util.Util.getTypeName
	    String[] extras = { "AnnotationType", "Class", "Enum", "Error", "Exception", "Interface" };
	    for (String s : extras) {
		if (results.contains("doclet." + s))
		    results.add("doclet." + s.toLowerCase());
	    }

	    // special handling for code strings synthesized in
	    // com.sun.tools.javadoc.Messager
	    results.add("javadoc.error.msg");
	    results.add("javadoc.note.msg");
	    results.add("javadoc.note.pos.msg");
	    results.add("javadoc.warning.msg");

	    return results;
	}
    }

    /**
     * Get the set of keys from the javadoc resource bundles.
     */
    Set&lt;String&gt; getResourceKeys() {
	Module jdk_javadoc = ModuleLayer.boot().findModule("jdk.javadoc").get();
	String[] names = { "com.sun.tools.javadoc.resources.javadoc", };
	Set&lt;String&gt; results = new TreeSet&lt;String&gt;();
	for (String name : names) {
	    ResourceBundle b = ResourceBundle.getBundle(name, jdk_javadoc);
	    results.addAll(b.keySet());
	}
	return results;
    }

    /**
     * Find keys in resource bundles which are probably no longer required.
     * A key is required if there is a string in the code that is a resource key,
     * or if the key is well-known according to various pragmatic rules.
     */
    void findDeadKeys(Set&lt;String&gt; codeKeys, Set&lt;String&gt; resourceKeys) {
	for (String rk : resourceKeys) {
	    if (codeKeys.contains(rk))
		continue;

	    error("Resource key not found in code: '" + rk + "'");
	}
    }

    /**
     * For all strings in the code that look like they might be
     * a resource key, verify that a key exists.
     */
    void findMissingKeys(Set&lt;String&gt; codeKeys, Set&lt;String&gt; resourceKeys) {
	for (String ck : codeKeys) {
	    if (resourceKeys.contains(ck))
		continue;
	    error("No resource for \"" + ck + "\"");
	}
    }

    JavaFileManager.Location findJavadocLocation(JavaFileManager fm) {
	JavaFileManager.Location[] locns = { StandardLocation.PLATFORM_CLASS_PATH, StandardLocation.CLASS_PATH };
	try {
	    for (JavaFileManager.Location l : locns) {
		JavaFileObject fo = fm.getJavaFileForInput(l, "com.sun.tools.javadoc.Main", JavaFileObject.Kind.CLASS);
		if (fo != null) {
		    System.err.println("found javadoc in " + l);
		    return l;
		}
	    }
	} catch (IOException e) {
	    throw new Error(e);
	}
	throw new IllegalStateException("Cannot find javadoc");
    }

    /**
     * Get the set of strings from a class file.
     * Only strings that look like they might be a resource key are returned.
     */
    void scan(JavaFileObject fo, Set&lt;String&gt; results) throws IOException {
	//System.err.println("scan " + fo.getName());
	InputStream in = fo.openInputStream();
	try {
	    ClassFile cf = ClassFile.read(in);
	    for (ConstantPool.CPInfo cpinfo : cf.constant_pool.entries()) {
		if (cpinfo.getTag() == ConstantPool.CONSTANT_Utf8) {
		    String v = ((ConstantPool.CONSTANT_Utf8_info) cpinfo).value;
		    if (v.matches("(doclet|main|javadoc|tag)\\.[A-Za-z0-9-_.]+"))
			results.add(v);
		}
	    }
	} catch (ConstantPoolException ignore) {
	} finally {
	    in.close();
	}
    }

}

