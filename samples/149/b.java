import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Hashtable;

class ReachableExcludesImpl implements ReachableExcludes {
    /**
     * @return true iff the given field is on the histlist of excluded
     *          fields.
     */
    public boolean isExcluded(String fieldName) {
	readFileIfNeeded();
	return methods.get(fieldName) != null;
    }

    private Hashtable&lt;String, String&gt; methods;
    private File excludesFile;
    private long lastModified;

    private void readFileIfNeeded() {
	if (excludesFile.lastModified() != lastModified) {
	    synchronized (this) {
		if (excludesFile.lastModified() != lastModified) {
		    readFile();
		}
	    }
	}
    }

    private void readFile() {
	long lm = excludesFile.lastModified();
	Hashtable&lt;String, String&gt; m = new Hashtable&lt;String, String&gt;();

	try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(excludesFile)))) {
	    String method;
	    while ((method = r.readLine()) != null) {
		m.put(method, method);
	    }
	    lastModified = lm;
	    methods = m; // We want this to be atomic
	} catch (IOException ex) {
	    System.out.println("Error reading " + excludesFile + ":  " + ex);
	}
    }

}

