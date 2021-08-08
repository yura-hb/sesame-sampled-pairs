import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;

class XMLGrammarPreparser {
    /**
     * Set the locale to use for messages.
     *
     * @param locale The locale object to use for localization of messages.
     *
     * @exception XNIException Thrown if the parser does not support the
     *                         specified locale.
     */
    public void setLocale(Locale locale) {
	fLocale = locale;
	fErrorReporter.setLocale(locale);
    }

    protected Locale fLocale;
    protected XMLErrorReporter fErrorReporter;

}

