class JToolTip extends JComponent implements Accessible {
    /**
     * Specifies the component that the tooltip describes.
     * The component &lt;code&gt;c&lt;/code&gt; may be &lt;code&gt;null&lt;/code&gt;
     * and will have no effect.
     * &lt;p&gt;
     * This is a bound property.
     *
     * @param c the &lt;code&gt;JComponent&lt;/code&gt; being described
     * @see JComponent#createToolTip
     */
    @BeanProperty(description = "Sets the component that the tooltip describes.")
    public void setComponent(JComponent c) {
	JComponent oldValue = this.component;

	component = c;
	firePropertyChange("component", oldValue, c);
    }

    JComponent component;

}

