import javax.swing.*;
import java.awt.Component;
import java.awt.Color;

class DefaultTableCellRenderer extends JLabel implements TableCellRenderer, Serializable {
    /**
     * Overridden for performance reasons.
     * See the &lt;a href="#override"&gt;Implementation Note&lt;/a&gt;
     * for more information.
     */
    public boolean isOpaque() {
	Color back = getBackground();
	Component p = getParent();
	if (p != null) {
	    p = p.getParent();
	}

	// p should now be the JTable.
	boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
	return !colorMatch && super.isOpaque();
    }

}

