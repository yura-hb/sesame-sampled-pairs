import java.util.Map;

class LoaderPool {
    /**
     * Returns the class loader for the named module
     */
    public Loader loaderFor(String name) {
	Loader loader = loaders.get(name);
	assert loader != null;
	return loader;
    }

    private final Map&lt;String, Loader&gt; loaders;

}

