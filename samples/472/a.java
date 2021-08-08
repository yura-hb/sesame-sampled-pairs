import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

class TreeTableCellRenderer extends JTree implements TableCellRenderer {
    /**
     * UpdateUI is overridden to set the colors of the Tree's renderer
     * to match that of the table.
     */
    @Override
    public void updateUI() {
	super.updateUI();
	// Make the tree's cell renderer use the table's cell selection
	// colors.
	final TreeCellRenderer tcr = getCellRenderer();
	if (tcr instanceof DefaultTreeCellRenderer) {
	    final DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tcr;
	    // For 1.1 uncomment this, 1.2 has a bug that will cause an
	    // exception to be thrown if the border selection color is
	    // null.
	    // renderer.setBorderSelectionColor(null);
	    renderer.setTextSelectionColor(UIManager.getColor("Table.selectionForeground"));
	    renderer.setBackgroundSelectionColor(UIManager.getColor("Table.selectionBackground"));
	}
    }

}

