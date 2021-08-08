import java.text.*;
import java.util.*;

class DateFormatter extends InternationalFormatter {
    /**
     * Returns the field that will be adjusted by adjustValue.
     */
    Object getAdjustField(int start, Map&lt;?, ?&gt; attributes) {
	Iterator&lt;?&gt; attrs = attributes.keySet().iterator();

	while (attrs.hasNext()) {
	    Object key = attrs.next();

	    if ((key instanceof DateFormat.Field)
		    && (key == DateFormat.Field.HOUR1 || ((DateFormat.Field) key).getCalendarField() != -1)) {
		return key;
	    }
	}
	return null;
    }

}

