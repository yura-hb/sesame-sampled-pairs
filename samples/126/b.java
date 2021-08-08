import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

class DesktopProperty implements ActiveValue {
    /**
     * Configures the value as appropriate for a defaults property in
     * the UIDefaults table.
     */
    protected Object configureValue(Object value) {
	if (value != null) {
	    if (value instanceof Color) {
		return new ColorUIResource((Color) value);
	    } else if (value instanceof Font) {
		return new FontUIResource((Font) value);
	    } else if (value instanceof UIDefaults.LazyValue) {
		value = ((UIDefaults.LazyValue) value).createValue(null);
	    } else if (value instanceof UIDefaults.ActiveValue) {
		value = ((UIDefaults.ActiveValue) value).createValue(null);
	    }
	}
	return value;
    }

}

