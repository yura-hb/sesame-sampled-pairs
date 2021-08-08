import javax.swing.*;

class JTreeTable extends JTable {
    /**
     * Overridden to pass the new rowHeight to the tree.
     */
    public void setRowHeight(int rowHeight) {
	super.setRowHeight(rowHeight);
	if (tree != null && tree.getRowHeight() != rowHeight) {
	    tree.setRowHeight(getRowHeight());
	}
    }

    /** A subclass of JTree. */
    protected TreeTableCellRenderer tree;

    class TreeTableCellRenderer extends JTree implements TableCellRenderer {
	/** A subclass of JTree. */
	protected TreeTableCellRenderer tree;

	/**
	 * Sets the row height of the tree, and forwards the row height to
	 * the table.
	 */
	public void setRowHeight(int rowHeight) {
	    if (rowHeight &gt; 0) {
		super.setRowHeight(rowHeight);
		if (JTreeTable.this != null && JTreeTable.this.getRowHeight() != rowHeight) {
		    JTreeTable.this.setRowHeight(getRowHeight());
		}
	    }
	}

    }

}

