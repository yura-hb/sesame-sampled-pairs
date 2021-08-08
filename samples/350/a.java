class Resources implements TextTranslator {
    /**
     * @param key
     *            Property key
     * @return the boolean value of the property resp. the default.
     */
    public boolean getBoolProperty(String key) {
	String boolProperty = getProperty(key);
	return Tools.safeEquals("true", boolProperty);
    }

    private FreeMindMain main;

    public String getProperty(String key) {
	return main.getProperty(key);
    }

}

