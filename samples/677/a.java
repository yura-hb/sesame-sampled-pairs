import javax.swing.JComboBox;

class JMonthChooser extends JPanel implements ItemListener, ChangeListener {
    /**
     * Enable or disable the JMonthChooser.
     * 
     * @param enabled
     *            the new enabled value
     */
    public void setEnabled(boolean enabled) {
	super.setEnabled(enabled);
	comboBox.setEnabled(enabled);

	if (spinner != null) {
	    spinner.setEnabled(enabled);
	}
    }

    private JComboBox&lt;String&gt; comboBox;
    private JSpinner spinner;

}

