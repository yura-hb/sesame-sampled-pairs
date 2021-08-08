import java.awt.Color;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

class JSpinField extends JPanel implements ChangeListener, CaretListener, ActionListener, FocusListener {
    /**
     * Is invoked when the spinner model changes
     * 
     * @param e
     *            the ChangeEvent
     */
    public void stateChanged(ChangeEvent e) {
	SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
	int value = model.getNumber().intValue();
	setValue(value);
    }

    protected JSpinner spinner;
    protected int value;
    protected int min;
    protected int max;
    /** the text (number) field */
    protected JTextField textField;

    /**
     * Sets the value. This is a bound property.
     * 
     * @param newValue
     *            the new value
     * 
     * @see #getValue
     */
    public void setValue(int newValue) {
	setValue(newValue, true, true);
	spinner.setValue(new Integer(value));
    }

    /**
     * Sets the value attribute of the JSpinField object.
     * 
     * @param newValue
     *            The new value
     * @param updateTextField
     *            true if text field should be updated
     */
    protected void setValue(int newValue, boolean updateTextField, boolean firePropertyChange) {
	int oldValue = value;
	if (newValue &lt; min) {
	    value = min;
	} else if (newValue &gt; max) {
	    value = max;
	} else {
	    value = newValue;
	}

	if (updateTextField) {
	    textField.setText(Integer.toString(value));
	    textField.setForeground(Color.black);
	}

	if (firePropertyChange) {
	    firePropertyChange("value", oldValue, value);
	}
    }

}

