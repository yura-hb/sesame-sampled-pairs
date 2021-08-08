class JDayChooser extends JPanel implements ActionListener, KeyListener, FocusListener {
    /**
     * Requests that the selected day also have the focus.
     */
    public void setFocus() {
	if (selectedDay != null) {
	    this.selectedDay.requestFocusInWindow();
	}
    }

    protected JButton selectedDay;

}

