import javax.swing.*;
import javax.swing.tree.*;

class JTreeTable extends JTable {
    class TreeTableCellRenderer extends JTree implements TableCellRenderer {
	/**
	 * updateUI is overridden to set the colors of the Tree's renderer
	 * to match that of the table.
	 */
	public void updateUI() {
	    super.updateUI();
	    // Make the tree's cell renderer use the table's cell selection
	    // colors.
	    TreeCellRenderer tcr = getCellRenderer();
	    if (tcr instanceof DefaultTreeCellRenderer) {
		DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
		// For 1.1 uncomment this, 1.2 has a bug that will cause an
		// exception to be thrown if the border selection color is
		// null.
		// dtcr.setBorderSelectionColor(null);
		dtcr.setTextSelectionColor(UIManager.getColor("Table.selectionForeground"));
		dtcr.setBackgroundSelectionColor(UIManager.getColor("Table.selectionBackground"));
	    }
	}

    }

}

