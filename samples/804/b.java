import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.ResourceBundle;

class Resources {
    /**
     * Initializes all non-final public static fields in the given class with
     * messages from a {@link ResourceBundle}.
     *
     * @param clazz the class containing the fields
     */
    public static void initializeMessages(Class&lt;?&gt; clazz, String rbName) {
	ResourceBundle rb = null;
	try {
	    rb = ResourceBundle.getBundle(rbName);
	} catch (MissingResourceException mre) {
	    // fall through, handled later
	}
	for (Field field : clazz.getFields()) {
	    if (isWritableField(field)) {
		String key = field.getName();
		String message = getMessage(rb, key);
		int mnemonicInt = findMnemonicInt(message);
		message = removeMnemonicAmpersand(message);
		message = replaceWithPlatformLineFeed(message);
		setFieldValue(field, message);
		MNEMONIC_LOOKUP.put(message, mnemonicInt);
	    }
	}
    }

    private static Map&lt;String, Integer&gt; MNEMONIC_LOOKUP = Collections
	    .synchronizedMap(new IdentityHashMap&lt;String, Integer&gt;());

    private static boolean isWritableField(Field field) {
	int modifiers = field.getModifiers();
	return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers);
    }

    /**
     * Returns the message corresponding to the key in the bundle or a text
     * describing it's missing.
     *
     * @param rb the resource bundle
     * @param key the key
     *
     * @return the message
     */
    private static String getMessage(ResourceBundle rb, String key) {
	if (rb == null) {
	    return "missing resource bundle";
	}
	try {
	    return rb.getString(key);
	} catch (MissingResourceException mre) {
	    return "missing message for key = \"" + key + "\" in resource bundle ";
	}
    }

    /**
     * Finds the mnemonic character in a message.
     *
     * The mnemonic character is the first character followed by the first
     * &lt;code&gt;&&lt;/code&gt; that is not followed by another &lt;code&gt;&&lt;/code&gt;.
     *
     * @return the mnemonic as an &lt;code&gt;int&lt;/code&gt;, or &lt;code&gt;0&lt;/code&gt; if it
     *         can't be found.
     */
    private static int findMnemonicInt(String s) {
	for (int i = 0; i &lt; s.length() - 1; i++) {
	    if (s.charAt(i) == '&') {
		if (s.charAt(i + 1) != '&') {
		    return lookupMnemonicInt(s.substring(i + 1, i + 2));
		} else {
		    i++;
		}
	    }
	}
	return 0;
    }

    /**
     * Removes the mnemonic identifier (&lt;code&gt;&&lt;/code&gt;) from a string unless
     * it's escaped by &lt;code&gt;&&&lt;/code&gt; or placed at the end.
     *
     * @param message the message
     *
     * @return a message with the mnemonic identifier removed
     */
    private static String removeMnemonicAmpersand(String message) {
	StringBuilder s = new StringBuilder();
	for (int i = 0; i &lt; message.length(); i++) {
	    char current = message.charAt(i);
	    if (current != '&' || i == message.length() - 1 || message.charAt(i + 1) == '&') {
		s.append(current);
	    }
	}
	return s.toString();
    }

    /**
     * Returns a {@link String} where all &lt;code&gt;\n&lt;/code&gt; in the &lt;text&gt; have
     * been replaced with the line separator for the platform.
     *
     * @param text the to be replaced
     *
     * @return the replaced text
     */
    private static String replaceWithPlatformLineFeed(String text) {
	return text.replace("\n", System.getProperty("line.separator"));
    }

    private static void setFieldValue(Field field, String value) {
	try {
	    field.set(null, value);
	} catch (IllegalArgumentException | IllegalAccessException e) {
	    throw new Error("Unable to access or set message for field " + field.getName());
	}
    }

    /**
     * Lookups the mnemonic for a key in the {@link KeyEvent} class.
     *
     * @param c the character to lookup
     *
     * @return the mnemonic as an &lt;code&gt;int&lt;/code&gt;, or &lt;code&gt;0&lt;/code&gt; if it
     *         can't be found.
     */
    private static int lookupMnemonicInt(String c) {
	try {
	    return KeyEvent.class.getDeclaredField("VK_" + c.toUpperCase()).getInt(null);
	} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
	    // Missing VK is okay
	    return 0;
	}
    }

}

