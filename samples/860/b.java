import com.sun.org.apache.xerces.internal.util.MessageFormatter;

class MalformedByteSequenceException extends CharConversionException {
    /**
     * &lt;p&gt;Returns the localized message for this exception.&lt;/p&gt;
     *
     * @return the localized message for this exception.
     */
    public String getMessage() {
	if (fMessage == null) {
	    fMessage = fFormatter.formatMessage(fLocale, fKey, fArguments);
	    // The references to the message formatter and locale
	    // aren't needed anymore so null them.
	    fFormatter = null;
	    fLocale = null;
	}
	return fMessage;
    }

    /** message text for this message, initially null **/
    private String fMessage;
    /** message formatter **/
    private MessageFormatter fFormatter;
    /** locale for error message **/
    private Locale fLocale;
    /** key for the error message **/
    private String fKey;
    /** replacement arguements for the error message **/
    private Object[] fArguments;

}

