class TreeSelectionEvent extends EventObject {
    /**
      * Returns the paths that have been added or removed from the selection.
      *
      * @return copy of the array of {@code TreePath} obects for this event.
      */
    public TreePath[] getPaths() {
	int numPaths;
	TreePath[] retPaths;

	numPaths = paths.length;
	retPaths = new TreePath[numPaths];
	System.arraycopy(paths, 0, retPaths, 0, numPaths);
	return retPaths;
    }

    /** Paths this event represents. */
    protected TreePath[] paths;

}

