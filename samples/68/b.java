import javax.swing.*;
import javax.swing.tree.*;

class JTreeTable extends JTable {
    /**
     * Overridden to message super and forward the method to the tree.
     * Since the tree is not actually in the component hieachy it will
     * never receive this unless we forward it in this manner.
     */
    public void updateUI() {
	super.updateUI();
	if (tree != null) {
	    tree.updateUI();
	    // Do this so that the editor is referencing the current renderer
	    // from the tree. The renderer can potentially change each time
	    // laf changes.
	    setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
	}
	// Use the tree's default foreground and background colors in the
	// table.
	LookAndFeel.installColorsAndFont(this, "Tree.background", "Tree.foreground", "Tree.font");
    }

    /** A subclass of JTree. */
    protected TreeTableCellRenderer tree;

    class TreeTableCellRenderer extends JTree implements TableCellRenderer {
	/** A subclass of JTree. */
	protected TreeTableCellRenderer tree;

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

    class TreeTableCellEditor extends DefaultCellEditor {
	/** A subclass of JTree. */
	protected TreeTableCellRenderer tree;

	public TreeTableCellEditor() {
	    super(new TreeTableTextField());
	}

    }

}

