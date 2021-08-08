import org.eclipse.jdt.internal.compiler.util.SimpleSet;

class Index {
    /**
    * Returns the document names that contain the given substring, if null then returns all of them.
    */
    public String[] queryDocumentNames(String substring) throws IOException {
	SimpleSet results;
	if (this.memoryIndex.hasChanged()) {
	    results = this.diskIndex.addDocumentNames(substring, this.memoryIndex);
	    this.memoryIndex.addDocumentNames(substring, results);
	} else {
	    results = this.diskIndex.addDocumentNames(substring, null);
	}
	if (results.elementSize == 0)
	    return null;

	String[] documentNames = new String[results.elementSize];
	int count = 0;
	Object[] paths = results.values;
	for (int i = 0, l = paths.length; i &lt; l; i++)
	    if (paths[i] != null)
		documentNames[count++] = (String) paths[i];
	return documentNames;
    }

    protected MemoryIndex memoryIndex;
    protected DiskIndex diskIndex;

}

