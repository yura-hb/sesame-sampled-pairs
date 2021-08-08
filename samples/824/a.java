import java.awt.Component;
import java.text.DateFormatSymbols;
import javax.swing.JComboBox;

class JMonthChooser extends JPanel implements ItemListener, ChangeListener {
    /**
     * Set the locale and initializes the new month names.
     * 
     * @param l
     *            the new locale value
     * 
     * @see #getLocale
     */
    public void setLocale(Locale l) {
	if (!initialized) {
	    super.setLocale(l);
	} else {
	    locale = l;
	    initNames();
	}
    }

    private boolean initialized;
    private Locale locale;
    private boolean localInitialize;
    private JComboBox&lt;String&gt; comboBox;
    private int month;

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

}

