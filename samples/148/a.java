import java.util.Calendar;

class JDayChooser extends JPanel implements ActionListener, KeyListener, FocusListener {
    /**
     * Sets the day. This is a bound property.
     * 
     * @param d
     *            the day
     * 
     * @see #getDay
     */
    public void setDay(int d) {
	if (d &lt; 1) {
	    d = 1;
	}

	int maxDaysInMonth = getDaysInMonth();

	if (d &gt; maxDaysInMonth) {
	    d = maxDaysInMonth;
	}

	day = d;

	if (selectedDay != null) {
	    selectedDay.setBackground(oldDayBackgroundColor);
	    selectedDay.repaint();
	}

	for (int i = 7; i &lt; 49; i++) {
	    if (days[i].getText().equals(Integer.toString(day))) {
		selectedDay = days[i];
		selectedDay.setBackground(selectedColor);
		break;
	    }
	}
	setFocus();

    }

    protected int day;
    protected JButton selectedDay;
    protected Color oldDayBackgroundColor;
    protected JButton[] days;
    protected Color selectedColor;
    protected Calendar calendar;

    public int getDaysInMonth() {
	return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Requests that the selected day also have the focus.
     */
    public void setFocus() {
	if (selectedDay != null) {
	    this.selectedDay.requestFocusInWindow();
	}
    }

}

