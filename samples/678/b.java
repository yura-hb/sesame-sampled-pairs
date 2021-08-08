import javax.swing.*;

abstract class AbstractColorChooserPanel extends JPanel {
    /**
     * Returns the color that is currently selected.
     * @return the &lt;code&gt;Color&lt;/code&gt; that is selected
     */
    protected Color getColorFromModel() {
	ColorSelectionModel model = getColorSelectionModel();
	return (model != null) ? model.getSelectedColor() : null;
    }

    /**
     *
     */
    private JColorChooser chooser;

    /**
      * Returns the model that the chooser panel is editing.
      * @return the &lt;code&gt;ColorSelectionModel&lt;/code&gt; model this panel
      *         is editing
      */
    public ColorSelectionModel getColorSelectionModel() {
	return (this.chooser != null) ? this.chooser.getSelectionModel() : null;
    }

}

