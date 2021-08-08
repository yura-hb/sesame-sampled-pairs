import javax.swing.JTable;
import javax.swing.LookAndFeel;

class TreeTable extends JTable {
    /**
     * Overridden to message super and forward the method to the tree.
     * Since the tree is not actually in the component hierarchy it will
     * never receive this unless we forward it in this manner.
     */
    @Override
    public void updateUI() {
	super.updateUI();
	if (tree != null) {
	    tree.updateUI();
	}
	// Use the tree's default foreground and background colors in the
	// table.
	LookAndFeel.installColorsAndFont(this, "Tree.background", "Tree.foreground", "Tree.font");
    }

    /** A subclass of JTree. */
    private final TreeTableCellRenderer tree;

}

