class AOTCompiledClass {
    /**
     * Get the list of methods which should be compiled.
     */
    ArrayList&lt;ResolvedJavaMethod&gt; getMethods() {
	ArrayList&lt;ResolvedJavaMethod&gt; m = methods;
	methods = null; // Free - it is not used after that.
	return m;
    }

    /**
     * List of all methods to be compiled.
     */
    private ArrayList&lt;ResolvedJavaMethod&gt; methods = new ArrayList&lt;&gt;();

}

