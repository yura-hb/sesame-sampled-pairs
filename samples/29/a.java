import javax.swing.JTable;
import javax.swing.JTree;

class TreeTableCellRenderer extends JTree implements TableCellRenderer {
    /**
     * Sets the row height of the tree, and forwards the row height to
     * the table.
     */
    @Override
    public void setRowHeight(int newRowHeight) {
	if (newRowHeight &gt; 0) {
	    super.setRowHeight(newRowHeight);
	    if (treeTable != null && treeTable.getRowHeight() != newRowHeight) {
		treeTable.setRowHeight(getRowHeight());
	    }
	}
    }

    /** Tree table to render. */
    private final TreeTable treeTable;

}

