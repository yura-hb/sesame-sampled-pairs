import javax.swing.*;

class PrintingStatus {
    /**
     * Duplicated from UIManager to make it visible
     */
    static int getInt(Object key, int defaultValue) {
	Object value = UIManager.get(key);
	if (value instanceof Integer) {
	    return ((Integer) value).intValue();
	}
	if (value instanceof String) {
	    try {
		return Integer.parseInt((String) value);
	    } catch (NumberFormatException nfe) {
	    }
	}
	return defaultValue;
    }

}

