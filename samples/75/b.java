import java.util.HashMap;

abstract class KeyResolverSpi {
    /**
     * Method engineSetProperty
     *
     * @param key
     * @param value
     */
    public void engineSetProperty(String key, String value) {
	if (properties == null) {
	    properties = new HashMap&lt;&gt;();
	}
	properties.put(key, value);
    }

    /** Field properties */
    protected java.util.Map&lt;String, String&gt; properties;

}

