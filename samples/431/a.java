import java.util.Properties;

class Nd4jContext implements Serializable {
    /**
     * Load the additional properties from an input stream and load all system properties
     *
     * @param inputStream
     */
    public void updateProperties(InputStream inputStream) {
	try {
	    conf.load(inputStream);
	    conf.putAll(System.getProperties());
	} catch (IOException e) {
	    log.warn("Error loading system properties from input stream", e);
	}
    }

    private Properties conf;

}

