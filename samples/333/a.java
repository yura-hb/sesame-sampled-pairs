import java.beans.PropertyChangeEvent;
import java.util.Calendar;

class JCalendar extends JPanel implements PropertyChangeListener {
    /**
     * JCalendar is a PropertyChangeListener, for its day, month and year
     * chooser.
     * 
     * @param evt
     *            the property change event
     */
    public void propertyChange(PropertyChangeEvent evt) {
	if (doingPropertyChanges) {
	    return;
	}
	doingPropertyChanges = true;
	try {
	    //			System.out.println("Property change in " +this.getClass().getSimpleName() + " of type " + evt.getPropertyName());
	    if (calendar != null) {
		Calendar c = (Calendar) calendar.clone();

		if (evt.getPropertyName().equals(JDayChooser.DAY_PROPERTY)) {
		    c = (Calendar) evt.getNewValue();
		    firePropertyChange(CALENDAR_PROPERTY, 0, c);
		} else if (evt.getPropertyName().equals(JMonthChooser.MONTH_PROPERTY)) {
		    c.set(Calendar.MONTH, ((Integer) evt.getNewValue()).intValue());
		    firePropertyChange(CALENDAR_PROPERTY, 0, c);
		} else if (evt.getPropertyName().equals(JYearChooser.YEAR_PROPERTY)) {
		    c.set(Calendar.YEAR, ((Integer) evt.getNewValue()).intValue());
		    firePropertyChange(CALENDAR_PROPERTY, 0, c);
		} else if (evt.getPropertyName().equals(DATE_PROPERTY)) {
		    c.setTime((Date) evt.getNewValue());
		    firePropertyChange(CALENDAR_PROPERTY, 0, c);
		}
	    }
	} finally {
	    doingPropertyChanges = false;
	}
    }

    private boolean doingPropertyChanges = false;
    private Calendar calendar;
    public static final String CALENDAR_PROPERTY = "calendar";
    public static final String DATE_PROPERTY = "date";

}

