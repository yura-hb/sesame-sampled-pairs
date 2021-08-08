import java.util.Calendar;
import java.util.GregorianCalendar;

class JDayChooser extends JPanel implements ActionListener, KeyListener, FocusListener {
    /**
     * JDayChooser is the ActionListener for all day buttons.
     * 
     * @param e
     *            the ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
	JButton button = (JButton) e.getSource();
	String buttonText = button.getText();
	int day = new Integer(buttonText).intValue();
	fire(day);
    }

    public static final String DAY_PROPERTY = "day";
    protected JYearChooser yearChooser = null;
    protected JMonthChooser monthChooser = null;
    protected int day;

    public void fire(int newDay) {
	GregorianCalendar tempCalendar = getTemporaryCalendar();
	tempCalendar.set(Calendar.DAY_OF_MONTH, newDay);
	firePropertyChange(DAY_PROPERTY, null, tempCalendar);
    }

    public GregorianCalendar getTemporaryCalendar() {
	GregorianCalendar tempCalendar = new GregorianCalendar(yearChooser.getYear(), monthChooser.getMonth(),
		getDay());
	return tempCalendar;
    }

    /**
     * Returns the selected day.
     * 
     * @return the day value
     * 
     * @see #setDay
     */
    public int getDay() {
	return day;
    }

}

