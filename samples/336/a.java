import java.util.HashSet;

class PathCollector extends IndexQueryRequestor {
    /**
     * Returns the paths that have been collected.
     */
    public String[] getPaths() {
	return (String[]) this.paths.toArray(new String[this.paths.size()]);
    }

    public HashSet paths = new HashSet(5);

}

