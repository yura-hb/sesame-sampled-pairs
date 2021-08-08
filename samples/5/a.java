class JMonthChooser extends JPanel implements ItemListener, ChangeListener {
    /**
     * Sets the font for this component.
     * 
     * @param font
     *            the desired &lt;code&gt;Font&lt;/code&gt; for this component
     */
    public void setFont(Font font) {
	if (comboBox != null) {
	    comboBox.setFont(font);
	}
	super.setFont(font);
    }

    private JComboBox&lt;String&gt; comboBox;

}

