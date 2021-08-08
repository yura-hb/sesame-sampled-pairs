import static java.text.DateFormatSymbols.*;

class SimpleDateFormat extends DateFormat {
    /**
     * Sets the date and time format symbols of this date format.
     *
     * @param newFormatSymbols the new date and time format symbols
     * @exception NullPointerException if the given newFormatSymbols is null
     * @see #getDateFormatSymbols
     */
    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols) {
	this.formatData = (DateFormatSymbols) newFormatSymbols.clone();
	useDateFormatSymbols = true;
    }

    /**
     * The symbols used by this formatter for week names, month names,
     * etc.  May not be null.
     * @serial
     * @see java.text.DateFormatSymbols
     */
    private DateFormatSymbols formatData;
    /**
     * Indicates whether this &lt;code&gt;SimpleDateFormat&lt;/code&gt; should use
     * the DateFormatSymbols. If true, the format and parse methods
     * use the DateFormatSymbols values. If false, the format and
     * parse methods call Calendar.getDisplayName or
     * Calendar.getDisplayNames.
     */
    transient boolean useDateFormatSymbols;

}

