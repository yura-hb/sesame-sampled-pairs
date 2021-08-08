import java.text.DateFormatSymbols;
import javax.swing.JComboBox;

class JMonthChooser extends JPanel implements ItemListener, ChangeListener {
    /**
     * Initializes the locale specific month names.
     */
    public void initNames() {
	localInitialize = true;

	DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
	String[] monthNames = dateFormatSymbols.getMonths();

	if (comboBox.getItemCount() == 12) {
	    comboBox.removeAllItems();
	}

	for (int i = 0; i &lt; 12; i++) {
	    comboBox.addItem(monthNames[i]);
	}

	localInitialize = false;
	comboBox.setSelectedIndex(month);
    }

    private boolean localInitialize;
    private Locale locale;
    private JComboBox&lt;String&gt; comboBox;
    private int month;

}

