import javax.swing.*;

abstract class AbstractColorChooserPanel extends JPanel {
    /**
      * Returns the model that the chooser panel is editing.
      * @return the &lt;code&gt;ColorSelectionModel&lt;/code&gt; model this panel
      *         is editing
      */
    public ColorSelectionModel getColorSelectionModel() {
	return (this.chooser != null) ? this.chooser.getSelectionModel() : null;
    }

    /**
     *
     */
    private JColorChooser chooser;

}

