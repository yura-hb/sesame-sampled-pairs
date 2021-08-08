class CheckstyleAntTask extends Task {
    /**
     * Set the class path.
     * @param classpath the path to locate classes
     */
    public void setClasspath(Path classpath) {
	if (this.classpath == null) {
	    this.classpath = classpath;
	} else {
	    this.classpath.append(classpath);
	}
    }

    /** Class path to locate class files. */
    private Path classpath;

}

