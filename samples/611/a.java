class JCalendar extends JPanel implements PropertyChangeListener {
    /**
     * Sets the background color.
     * 
     * @param bg
     *            the new background
     */
    public void setBackground(Color bg) {
	super.setBackground(bg);

	if (dayChooser != null) {
	    dayChooser.setBackground(bg);
	}
    }

    /** the day chooser */
    protected JDayChooser dayChooser;

}

