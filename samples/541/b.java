import java.io.*;
import java.util.*;

class CheckExamples {
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

    boolean isValidExample(File f) {
	return (f.isDirectory() && f.list().length &gt; 0) || (f.isFile() && f.getName().endsWith(".java"));
    }

}

