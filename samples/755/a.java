import java.util.Calendar;

class JCalendar extends JPanel implements PropertyChangeListener {
    /**
     * Sets the date. Fires the property change "date".
     * 
     * @param date
     *            the new date.
     * @throws NullPointerException
     *             - if tha date is null
     */
    public void setDate(Date date) {
	Date oldDate = calendar.getTime();
	calendar.setTime(date);
	int year = calendar.get(Calendar.YEAR);
	int month = calendar.get(Calendar.MONTH);
	int day = calendar.get(Calendar.DAY_OF_MONTH);

	yearChooser.setYear(year);
	monthChooser.setMonth(month);
	dayChooser.setCalendar(calendar);
	dayChooser.setDay(day);

	firePropertyChange(DATE_PROPERTY, oldDate, date);
    }

    private Calendar calendar;
    /** the year chhoser */
    protected JYearChooser yearChooser;
    /** the month chooser */
    protected JMonthChooser monthChooser;
    /** the day chooser */
    protected JDayChooser dayChooser;
    public static final String DATE_PROPERTY = "date";

}

