class LocaleDataProvider {
    /**
     * Utility method for loading a resource bundle in jdk.localedata.
     */
    static ResourceBundle loadResourceBundle(String bundleName) {
	Class&lt;?&gt; c = Class.forName(LocaleDataProvider.class.getModule(), bundleName);
	if (c != null && ResourceBundle.class.isAssignableFrom(c)) {
	    try {
		@SuppressWarnings({ "unchecked", "deprecation" })
		ResourceBundle rb = ((Class&lt;ResourceBundle&gt;) c).newInstance();
		return rb;
	    } catch (InstantiationException | IllegalAccessException e) {
		throw new InternalError(e);
	    }
	}
	return null;
    }

}

