class DocEnv {
    /**
     * Set the locale.
     */
    public void setLocale(String localeName) {
	// create locale specifics
	doclocale = new DocLocale(this, localeName, breakiterator);
	// update Messager if locale has changed.
	messager.setLocale(doclocale.locale);
    }

    DocLocale doclocale;
    /** True if we are using a sentence BreakIterator. */
    boolean breakiterator;
    private final Messager messager;

}

