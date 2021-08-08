import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

class JMonthChooser extends JPanel implements ItemListener, ChangeListener {
    /**
     * Is invoked if the state of the spnner changes.
     * 
     * @param e
     *            the change event.
     */
    public void stateChanged(ChangeEvent e) {
	SpinnerNumberModel model = (SpinnerNumberModel) ((JSpinner) e.getSource()).getModel();
	int value = model.getNumber().intValue();

	//		changeMonth(increase);
	firePropertyChange(MONTH_PROPERTY, 0, value);

    }

    public static final String MONTH_PROPERTY = "month";

}

