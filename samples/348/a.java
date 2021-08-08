import java.awt.event.ItemEvent;
import javax.swing.JComboBox;

class JMonthChooser extends JPanel implements ItemListener, ChangeListener {
    /**
     * The ItemListener for the months.
     * 
     * @param e
     *            the item event
     */
    public void itemStateChanged(ItemEvent e) {
	if (e.getStateChange() == ItemEvent.SELECTED) {
	    int index = comboBox.getSelectedIndex();

	    if ((index &gt;= 0) && (index != month)) {
		setMonth(index, false);
	    }
	}
    }

    private JComboBox&lt;String&gt; comboBox;
    private int month;
    private boolean initialized;
    private boolean localInitialize;
    private JDayChooser dayChooser;
    public static final String MONTH_PROPERTY = "month";

    /**
     * Sets the month attribute of the JMonthChooser object. Fires a property
     * change "month".
     * 
     * @param newMonth
     *            the new month value
     * @param select
     *            true, if the month should be selcted in the combo box.
     */
    private void setMonth(int newMonth, boolean select) {
	if (!initialized || localInitialize) {
	    return;
	}

	int oldMonth = month;
	month = newMonth;

	if (select) {
	    comboBox.setSelectedIndex(month);
	}

	if (dayChooser != null) {
	    dayChooser.setMonth(month);
	}

	firePropertyChange(MONTH_PROPERTY, oldMonth, month);
    }

}

