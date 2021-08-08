class JCalendar extends JPanel implements PropertyChangeListener {
    /**
     * Sets the week of year visible.
     * 
     * @param weekOfYearVisible
     *            true, if weeks of year shall be visible
     */
    public void setWeekOfYearVisible(boolean weekOfYearVisible) {
	dayChooser.setWeekOfYearVisible(weekOfYearVisible);
	setLocale(locale); // hack for doing complete new layout :)
    }

    /** the day chooser */
    protected JDayChooser dayChooser;
    /** the locale */
    protected Locale locale;
    private boolean initialized = false;
    /** the month chooser */
    protected JMonthChooser monthChooser;

    /**
     * Sets the locale property. This is a bound property.
     * 
     * @param l
     *            the new locale value
     * 
     * @see #getLocale
     */
    public void setLocale(Locale l) {
	if (!initialized) {
	    super.setLocale(l);
	} else {
	    Locale oldLocale = locale;
	    locale = l;
	    dayChooser.setLocale(locale);
	    monthChooser.setLocale(locale);
	    firePropertyChange("locale", oldLocale, locale);
	}
    }

}

