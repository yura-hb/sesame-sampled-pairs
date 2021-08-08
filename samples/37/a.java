import javax.swing.JTable;
import javax.swing.JTree;

class TreeTable extends JTable {
    /**
     * Overridden to pass the new rowHeight to the tree.
     */
    @Override
    public void setRowHeight(int newRowHeight) {
	super.setRowHeight(newRowHeight);
	if (tree != null && tree.getRowHeight() != newRowHeight) {
	    tree.setRowHeight(getRowHeight());
	}
    }

    /** A subclass of JTree. */
    private final TreeTableCellRenderer tree;

}

