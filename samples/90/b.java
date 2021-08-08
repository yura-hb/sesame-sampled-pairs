import java.io.*;
import java.util.*;

class CheckExamples {
    /**
     * Run the test.
     */
    void run() throws Exception {
	Set&lt;Example&gt; examples = getExamples();

	Set&lt;String&gt; notYetList = getNotYetList();
	Set&lt;String&gt; declaredKeys = new TreeSet&lt;String&gt;();
	for (Example e : examples) {
	    Set&lt;String&gt; e_decl = e.getDeclaredKeys();
	    Set&lt;String&gt; e_actual = e.getActualKeys();
	    for (String k : e_decl) {
		if (!e_actual.contains(k))
		    error("Example " + e + " declares key " + k + " but does not generate it");
	    }
	    for (String k : e_actual) {
		if (!e_decl.contains(k))
		    error("Example " + e + " generates key " + k + " but does not declare it");
	    }
	    for (String k : e.getDeclaredKeys()) {
		if (notYetList.contains(k))
		    error("Example " + e + " declares key " + k + " which is also on the \"not yet\" list");
		declaredKeys.add(k);
	    }
	}

	Module jdk_compiler = ModuleLayer.boot().findModule("jdk.compiler").get();
	ResourceBundle b = ResourceBundle.getBundle("com.sun.tools.javac.resources.compiler", jdk_compiler);
	Set&lt;String&gt; resourceKeys = new TreeSet&lt;String&gt;(b.keySet());

	for (String dk : declaredKeys) {
	    if (!resourceKeys.contains(dk))
		error("Key " + dk + " is declared in tests but is not a valid key in resource bundle");
	}

	for (String nk : notYetList) {
	    if (!resourceKeys.contains(nk))
		error("Key " + nk + " is declared in not-yet list but is not a valid key in resource bundle");
	}

	for (String rk : resourceKeys) {
	    if (!declaredKeys.contains(rk) && !notYetList.contains(rk))
		error("Key " + rk + " is declared in resource bundle but is not in tests or not-yet list");
	}

	System.err.println(examples.size() + " examples checked");
	System.err.println(notYetList.size() + " keys on not-yet list");

	Counts declaredCounts = new Counts(declaredKeys);
	Counts resourceCounts = new Counts(resourceKeys);
	List&lt;String&gt; rows = new ArrayList&lt;String&gt;(Arrays.asList(Counts.prefixes));
	rows.add("other");
	rows.add("total");
	System.err.println();
	System.err.println(String.format("%-14s %15s %15s %4s", "prefix", "#keys in tests", "#keys in javac", "%"));
	for (String p : rows) {
	    int d = declaredCounts.get(p);
	    int r = resourceCounts.get(p);
	    System.err.print(String.format("%-14s %15d %15d", p, d, r));
	    if (r != 0)
		System.err.print(String.format(" %3d%%", (d * 100) / r));
	    System.err.println();
	}

	if (errors &gt; 0)
	    throw new Exception(errors + " errors occurred.");
    }

    int errors;

    /**
     * Get the complete set of examples to be checked.
     */
    Set&lt;Example&gt; getExamples() {
	Set&lt;Example&gt; results = new TreeSet&lt;Example&gt;();
	File testSrc = new File(System.getProperty("test.src"));
	File examples = new File(testSrc, "examples");
	for (File f : examples.listFiles()) {
	    if (isValidExample(f))
		results.add(new Example(f));
	}
	return results;
    }

    /**
     * Get the contents of the "not-yet" list.
     */
    Set&lt;String&gt; getNotYetList() {
	Set&lt;String&gt; results = new TreeSet&lt;String&gt;();
	File testSrc = new File(System.getProperty("test.src"));
	File notYetList = new File(testSrc, "examples.not-yet.txt");
	try {
	    String[] lines = read(notYetList).split("[\r\n]");
	    for (String line : lines) {
		int hash = line.indexOf("#");
		if (hash != -1)
		    line = line.substring(0, hash).trim();
		if (line.matches("[A-Za-z0-9-_.]+"))
		    results.add(line);
	    }
	} catch (IOException e) {
	    throw new Error(e);
	}
	return results;
    }

    /**
     * Report an error.
     */
    void error(String msg) {
	System.err.println("Error: " + msg);
	errors++;
    }

    boolean isValidExample(File f) {
	return (f.isDirectory() && f.list().length &gt; 0) || (f.isFile() && f.getName().endsWith(".java"));
    }

    /**
     * Read the contents of a file.
     */
    String read(File f) throws IOException {
	byte[] bytes = new byte[(int) f.length()];
	DataInputStream in = new DataInputStream(new FileInputStream(f));
	try {
	    in.readFully(bytes);
	} finally {
	    in.close();
	}
	return new String(bytes);
    }

    class Counts {
	int errors;

	Counts(Set&lt;String&gt; keys) {
	    nextKey: for (String k : keys) {
		for (String p : prefixes) {
		    if (k.startsWith(p)) {
			inc(p);
			continue nextKey;
		    }
		}
		inc("other");
	    }
	    table.put("total", keys.size());
	}

	int get(String p) {
	    Integer i = table.get(p);
	    return (i == null ? 0 : i);
	}

	void inc(String p) {
	    Integer i = table.get(p);
	    table.put(p, (i == null ? 1 : i + 1));
	}

    }

}

