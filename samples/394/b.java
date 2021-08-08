import java.awt.*;
import javax.swing.*;

class BasicComboBoxUI extends ComboBoxUI {
    /**
     * Selects the next item in the list.  It won't change the selection if the
     * currently selected item is already the last item.
     */
    protected void selectNextPossibleValue() {
	int si;

	if (comboBox.isPopupVisible()) {
	    si = listBox.getSelectedIndex();
	} else {
	    si = comboBox.getSelectedIndex();
	}

	if (si &lt; comboBox.getModel().getSize() - 1) {
	    listBox.setSelectedIndex(si + 1);
	    listBox.ensureIndexIsVisible(si + 1);
	    if (!isTableCellEditor) {
		if (!(UIManager.getBoolean("ComboBox.noActionOnKeyNavigation") && comboBox.isPopupVisible())) {
		    comboBox.setSelectedIndex(si + 1);
		}
	    }
	    comboBox.repaint();
	}
    }

    /**
     * The instance of {@code JComboBox}.
     */
    protected JComboBox&lt;Object&gt; comboBox;
    /**
     * This list is for drawing the current item in the combo box.
     */
    protected JList&lt;Object&gt; listBox;
    private boolean isTableCellEditor = false;

}

