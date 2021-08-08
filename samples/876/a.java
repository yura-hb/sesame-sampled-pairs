import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

class XdocUtil {
    /**
     * Gets xdocs documentation file paths.
     * @param files list of all xdoc files
     * @return a list of xdocs config file paths.
     */
    public static Set&lt;Path&gt; getXdocsConfigFilePaths(Set&lt;Path&gt; files) {
	final Set&lt;Path&gt; xdocs = new HashSet&lt;&gt;();
	for (Path entry : files) {
	    final String fileName = entry.getFileName().toString();
	    if (fileName.startsWith("config_")) {
		xdocs.add(entry);
	    }
	}
	return xdocs;
    }

}

