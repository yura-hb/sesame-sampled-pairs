import com.puppycrawl.tools.checkstyle.api.AutomaticBean;

class Checker extends AutomaticBean implements MessageDispatcher, RootModule {
    /**
     * Sets cache file.
     * @param fileName the cache file.
     * @throws IOException if there are some problems with file loading.
     */
    public void setCacheFile(String fileName) throws IOException {
	final Configuration configuration = getConfiguration();
	cacheFile = new PropertyCacheFile(configuration, fileName);
	cacheFile.load();
    }

    /** Cache file. **/
    private PropertyCacheFile cacheFile;

}

