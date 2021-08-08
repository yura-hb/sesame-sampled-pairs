import java.awt.Component;

class TreeTableCellRenderer extends JTree implements TableCellRenderer {
    /**
     * This is overridden to set the height to match that of the JTable.
     */
    @Override
    public void setBounds(int x, int y, int w, int h) {
	super.setBounds(x, 0, w, treeTable.getHeight());
    }

    /** Tree table to render. */
    private final TreeTable treeTable;

}

